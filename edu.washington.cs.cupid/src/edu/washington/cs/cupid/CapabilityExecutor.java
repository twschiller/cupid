package edu.washington.cs.cupid;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.MalformedCapabilityException;
import edu.washington.cs.cupid.capability.TypeException;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.internal.CupidJobStatus;
import edu.washington.cs.cupid.internal.Utility;
import edu.washington.cs.cupid.jobs.ImmediateJob;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.preferences.PreferenceConstants;
import edu.washington.cs.cupid.shadow.IShadowJob;
import edu.washington.cs.cupid.shadow.ShadowJavaJob;
import edu.washington.cs.cupid.shadow.ShadowResourceJob;

/**
 * <p>A singleton executor of Cupid capabilities. This class cannot be instantiated or subclassed by clients; 
 * all functionality is provided by static methods. Features include:</p>
 * 
 * <ul>
 *   <li>synchronously executing a capability</li>
 *   <li>asynchronously executing a capability</li>
 * </ul>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @see {@link CupidPlatform}
 */
public final class CapabilityExecutor implements IResourceChangeListener, IPropertyChangeListener {

	// TODO clean up locks
	
	/**
	 * <code>true</code> iff job status should be logged
	 */
	private boolean logJobStatus;
	
	/**
	 * <code>true</code> iff cache status (e.g., hits and misses) should be logged
	 */
	private boolean logCacheStatus;
	
	/**
	 * Cupid result caches: Input -> { Capability -> Result }
	 */
	private final Cache<Object, Cache<Object,Object>> resultCaches;
	
	/**
	 * Running jobs: Input -> { Capability -> Result }
	 */
	@SuppressWarnings("rawtypes")
	private final Table<Object, ICapability, CapabilityJob> running;
	
	/**
	 * Jobs that have been canceled <i>by this executor</i>.
	 */
	@SuppressWarnings("rawtypes")
	private final Set<CapabilityJob> canceling;
	
	private final Set<IInvalidationListener> cacheListeners = Sets.newIdentityHashSet();
	
	/**
	 * Singleton instance
	 */
	private static CapabilityExecutor instance = null;
	
	/**
	 * Monitor lock for instance creation
	 */
	private static Boolean instanceMonitor = false;
	
	private final JobResultCacher cacher = new JobResultCacher();
	private final JobLogger logger = new JobLogger();
	private final JobReaper reaper = new JobReaper();
	
	private static final CapabilityCacheFactory cacheFactory = new CapabilityCacheFactory();
	
	private CapabilityExecutor(){
		resultCaches = CacheBuilder.newBuilder().build();
		running = HashBasedTable.create();
		canceling = Sets.newIdentityHashSet();
		
		IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
		logJobStatus = preferences.getBoolean(PreferenceConstants.P_JOB_STATUS_LOGGING);
		logCacheStatus = preferences.getBoolean(PreferenceConstants.P_CACHE_STATUS_LOGGING);
		preferences.addPropertyChangeListener(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_JOB_STATUS_LOGGING)){
			logJobStatus = (Boolean) event.getNewValue();
		}else if (event.getProperty().equals(PreferenceConstants.P_CACHE_STATUS_LOGGING)){
			logCacheStatus = (Boolean) event.getNewValue();
		}	
	}
	
	/**
	 * @return the singleton instance
	 */
	private static CapabilityExecutor getInstance(){
		synchronized(instanceMonitor){
			if (instance == null){
				instance = new CapabilityExecutor();
				ResourcesPlugin.getWorkspace().addResourceChangeListener(instance);
			}
			return instance;
		}
	}

	/**
	 * Initializes the default capability result cache for an input
	 * @author Todd Schiller (tws@cs.washington.edu)
	 */
	private static class CapabilityCacheFactory implements Callable<Cache<Object,Object>>{
		@Override
		public Cache<Object, Object> call() throws Exception {
			return CacheBuilder
					.newBuilder()
					.build();
		}
	}
	
	/**
	 * Returns the cached result, or <code>null</code> if the result is not cached
	 * @param capability the capability
	 * @param input the input
	 * @return the cached result, or <code>null</code> if the result is not cached
	 */
	@SuppressWarnings("unchecked") // FP: result is checked dynamically using capabilities return type
	private <I,T> T getIfPresent(ICapability<I,T> capability, I input){
		synchronized(resultCaches){
			try {
				Cache<Object,Object> cCache = resultCaches.get(input, cacheFactory);
				
				Object cached = cCache.getIfPresent(capability);
				
				if (cached == null){
					return null;
				}else if (capability.getReturnType().isAssignableFrom(cached.getClass())){
					return (T) cached;
				}else{
					throw new TypeException(capability.getReturnType(), TypeToken.of(cached.getClass()));
				}
			} catch (ExecutionException e) {
				CupidActivator.getDefault().log(new CupidJobStatus(capability.getJob(input), Status.WARNING, "error creating capability cache"));
				return null;
			}
		}
	}
	
	/**
	 * Synchronously execute a capability
	 * @param capability the capability
	 * @param input the input
	 * @return the result of the capability
	 * @throws RuntimeException iff the job is interrupted, or the capability throws an exception
	 * @deprecated execute capabilities asynchronously instead
	 */
	public static <I,T> T exec(ICapability<I,T> capability, I input){
		CapabilityExecutor executor = getInstance();
		
		T cached = executor.getIfPresent(capability, input);
		
		if (cached == null){
			CapabilityJob<I,T> job = capability.isPure() ? capability.getJob(input) : addSetup(capability, input);
			job.addJobChangeListener(executor.logger);
			job.schedule();
			
			try {
				job.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			CapabilityStatus<T> result = (CapabilityStatus<T>) job.getResult();
			
			if (result.isOK()){
				if (!capability.isTransient()){
					getInstance().resultCaches.getIfPresent(input).put(capability, result);
				}
				return result.value();
			}else{
				throw new RuntimeException(result.getException());
			}
		}else{
			if (executor.logCacheStatus){
				CupidActivator.getDefault().log(new CupidJobStatus(capability.getJob(input), Status.INFO, "cache hit"));
			}
		
			return cached;
		}
	}
	
	/**
	 * Asynchronously execute a capability.
	 * @param capability the capability
	 * @param input the input
	 * @param family the job family (used for job cancellation)
	 * @param callback the job listener
	 */
	public static <I,T> void asyncExec(ICapability<I,T> capability, I input, Object family, IJobChangeListener callback){
		CapabilityExecutor executor = getInstance();
		
		T cached = executor.getIfPresent(capability, input);
		
		CapabilityJob<I,T> job;
		
		synchronized(executor.running){
			synchronized (executor.canceling){
				CapabilityJob<I,T> existing = executor.running.get(input, capability);

				if (cached != null){ // CACHED
					if (executor.logCacheStatus){
						
						if (capability.getParameterType().equals(ICapability.UNIT_TOKEN)){
							CupidActivator.getDefault().log(new CupidJobStatus(capability.getJob(null), Status.INFO, "cache hit"));
						}else{
							CupidActivator.getDefault().log(new CupidJobStatus(capability.getJob(input), Status.INFO, "cache hit"));
						}
					}
					job = new ImmediateJob<I,T>(capability, input, cached);		

				}else if (existing != null && !executor.canceling.contains(existing)){ // ALREADY RUNNING
					// FIXME this allows jobs to be spuriously killed by other requesters
					// TODO the job might finish before we get a change to add the callback?
					job = existing;

					if (executor.logJobStatus){
						CupidActivator.getDefault().log(new CupidJobStatus(job, Status.INFO, "attaching new listener"));
					}

				}else{ // SPAWN NEW JOB	
					
					if (capability.getParameterType().equals(ICapability.UNIT_TOKEN)){
						job = capability.isPure() ? capability.getJob(null) : addSetup(capability, input);
					}else{
						job = capability.isPure() ? capability.getJob(input) : addSetup(capability, input);
					}
					
					if (job == null){
						job = new ImmediateJob(capability, input, new MalformedCapabilityException(capability, "Capability returned null job"));
					}
					
					if (!capability.isTransient()){
						job.addJobChangeListener(executor.cacher);
					}
					job.addJobChangeListener(executor.logger);
					job.addJobChangeListener(executor.reaper);
				}
			}
		}
		
		job.addJobChangeListener(callback);
		
		if (family != null){
			job.addFamily(family);
		}
		
		job.schedule();
	}
	
	private static <I,T> CapabilityJob<I,T> addSetup(final ICapability<I,T> capability, final I input){
		return new CapabilityJob<I,T>(capability, input){
			@Override
			protected CapabilityStatus<T> run(IProgressMonitor monitor) {
				IShadowJob<?> jSetup = null;
				
				monitor.subTask("Create Shadow Project");
				
				if (input instanceof IJavaElement){
					jSetup = new ShadowJavaJob((IJavaElement) input);
				}else if (input instanceof IResource){
					jSetup = new ShadowResourceJob((IResource) input);
				}else{
					return CapabilityStatus.makeError(new RuntimeException("No setup defined for type " + input.getClass()));
				}
				
				((Job) jSetup).schedule();
				try {
					((Job) jSetup).join();
				} catch (InterruptedException e) {
					return CapabilityStatus.makeError(e);
				}
				
				Job jMain = capability.getJob((I) jSetup.get());
				
				jMain.schedule();
				try {
					jMain.join();
				} catch (InterruptedException e) {
					return CapabilityStatus.makeError(e);
				}
				return (CapabilityStatus<T>) jMain.getResult();
			}
		};
	}
	
	/**
	 * Walks a resource delta to invalidate cache lines
	 * @author Todd Schiller (tws@cs.washington.edu)
	 */
	private class InvalidationVisitor implements IResourceDeltaVisitor{
		private Set<Object> invalidated = Sets.newIdentityHashSet();
		
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (delta.getAffectedChildren().length == 0){
				IResource resource = delta.getResource();
				if (resource != null && interesting(delta)){
					
					// invalidate cache lines
					Set<Object> invalidCacheEntries = Sets.newHashSet();
					for (Object input : resultCaches.asMap().keySet()){
						if (resource.isConflicting(schedulingRule(input))){
							invalidCacheEntries.add(input);
						}
					}
					
					if (!invalidCacheEntries.isEmpty()){
						if (logCacheStatus){
							CupidActivator.getDefault().logInformation(
								"Invalidating resource " + resource.getFullPath().toPortableString() + " ejects " + invalidCacheEntries.size() + " entries");
						}
						invalidated.addAll(invalidCacheEntries);
						resultCaches.invalidateAll(invalidCacheEntries);
					}
					
					// cancel obsolete jobs
					for (Object input : running.rowKeySet()){
						if (resource.isConflicting(schedulingRule(input))){
							for (CapabilityJob<?,?> job : running.row(input).values()){
								job.cancel();
							}
						}
					}
				}
			}
			return true;
		}
		
		/**
		 * @param delta the delta
		 * @return <code>true</code> iff the delta causes resources to be invalidated
		 */
		private boolean interesting(IResourceDelta delta){
			return (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.TYPE)) != 0;
		}
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getDelta() != null){
			synchronized(resultCaches){
				synchronized(running){
					synchronized(canceling){
						try {
							InvalidationVisitor v = new InvalidationVisitor();
							event.getDelta().accept(v);
							
							synchronized(cacheListeners){
								for (IInvalidationListener listener : cacheListeners){
									listener.onResourceChange(v.invalidated, event);
								}
							}
							
						} catch (CoreException e) {
							CupidActivator.getDefault().logError("Error invalidating cache result", e);
							throw new RuntimeException("Error invalidating result cache", e);
						}
					}
				}
			}
		}
	}
	
	private class JobResultCacher extends NullJobListener{
		@Override
		public void done(IJobChangeEvent event) {
			CapabilityJob<?,?> job = (CapabilityJob<?,?>) event.getJob();
			synchronized(resultCaches){
				Object value = ((CapabilityStatus<?>) job.getResult()).value();
				
				if (value != null){
					if (logCacheStatus){
						CupidActivator.getDefault().log(new CupidJobStatus(job, Status.INFO, "caching result\t" + job.getCapability().hashCode()));
					}
					
					try {
						resultCaches.get(job.getInput(), cacheFactory).put(job.getCapability(), value);
					} catch (ExecutionException e) {
						CupidActivator.getDefault().logError("Error adding cache result", e);
						throw new RuntimeException("Error adding result cache", e);
					}
				}
			}
		}
	}	
	
	private class JobReaper extends NullJobListener{
		@Override
		public void done(IJobChangeEvent event) {
			synchronized(running){
				synchronized(canceling){
					CapabilityStatus<?> result = (CapabilityStatus<?>) event.getResult();
					CapabilityJob<?,?> job = (CapabilityJob<?,?>) event.getJob();
					running.remove(job.getInput(), result.value());
					canceling.add(job);
				}
			}
		}
	}
		
	private class JobLogger implements IJobChangeListener{

		private void log(Job job, String message){
			if (logJobStatus){
				CupidActivator.getDefault().log(new CupidJobStatus(job, Status.INFO, message));
			}
		}
		
		@Override
		public void aboutToRun(IJobChangeEvent event) {
			log (event.getJob(), "about to run");
		}

		@Override
		public void awake(IJobChangeEvent event) {
			log (event.getJob(), "awake");
		}

		@Override
		public void done(IJobChangeEvent event) {
			log (event.getJob(), "done");
		}

		@Override
		public void running(IJobChangeEvent event) {
			log (event.getJob(), "running");
		}

		@Override
		public void scheduled(IJobChangeEvent event) {
			log (event.getJob(), "scheduled");
		}

		@Override
		public void sleeping(IJobChangeEvent event) {
			log (event.getJob(), "sleeping");
		}	
	}
	
	/**
	 * @param capability the capability
	 * @param argumentType the argument type
	 * @return <code>true</code> iff an argument of type <code>argumentType</code> can be supplied as the argument
	 * for <code>capability</code>
	 */
	public static boolean isCompatible(ICapability<?,?> capability, TypeToken<?> argumentType){
		
		if (capability == null){
			throw new IllegalArgumentException("Capability cannot be null");
		}else if (capability.getParameterType() == null){
			throw new IllegalArgumentException("Capability " + capability.getName() + " has null parameter type");
		}
		
		Type paramType = capability.getParameterType().getType();
		
		if (capability.getParameterType().getRawType().equals(Integer.class) && argumentType.getRawType().equals(int.class)){
			return true;
		}else if (capability.getParameterType().equals(ICapability.UNIT_TOKEN)){
			return true;
		}else if (paramType instanceof ParameterizedType){
			if (capability.getParameterType().getRawType().isAssignableFrom(argumentType.getRawType())){
				// check if type is all variables (i.e., fully generic)
				for (Type arg : ((ParameterizedType) paramType).getActualTypeArguments()){
					if (!(arg instanceof TypeVariable)){
						return capability.getParameterType().isAssignableFrom(argumentType);
					}
				}
				return true;
			}else{
				return false;
			}
		}else{
			return capability.getParameterType().isAssignableFrom(argumentType);
		}
	}
	
	public static boolean isResultCompatible(ICapability<?,?> capability, TypeToken<?> resultType){
		return resultType.isAssignableFrom(capability.getReturnType());
	}
	
	/**
	 * @param capability the capability
	 * @param argument the argument
	 * @return  <code>true</code> iff <code>argument</code> can be supplied as the argument
	 * for <code>capability</code>
	 */
	public static boolean isCompatible(ICapability<?,?> capability, Object argument){
		if (isCompatible(capability, TypeToken.of(argument.getClass()))){
			return true;
		}else{
			for (Object other : corresponding(argument)){
				if (isCompatible(capability, other.getClass())){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param capability the capability
	 * @param argument the suggested argument
	 * @return <code>argument</code> iff it is compatible with <code>capability</code>; 
	 * a corresponding compatible argument, otherwise
	 * @throws IllegalArgument Exception iff a compatible argument cannot be found
	 * @see {@link Utility#isCompatible(ICapability, Object)}
	 * @see {@link Utility#corresponding(Object)}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getCompatible(ICapability<T,?> capability, Object argument){
		if (isCompatible(capability, TypeToken.of(argument.getClass()))){
			return (T) argument;
		}else{
			for (Object other : corresponding(argument)){
				if (isCompatible(capability, other.getClass())){
					return (T) other;
				}
			}
		}
		throw new IllegalArgumentException("Argument is not compatible with capability");
	}
	
	/**
	 * @param o an object
	 * @deprecated an extension point will be exposed to provide scheduling rules
	 * @return the scheduling rule associated with the object
	 */
	public static ISchedulingRule schedulingRule(Object o){
		// TODO expose extension point for scheduling rules
		
		if (o instanceof ISchedulingRule){
			return (ISchedulingRule) o;
		}else if (o instanceof IJavaElement){
			return ((IJavaElement) o).getSchedulingRule();
		}else{
			throw new IllegalArgumentException("Scheduling rule not defined for type " + o.getClass().getName());
		}
	}
	
	/**
	 * Returns objects (e.g., {@link IResource}s) that are in one-to-one corresponding
	 * with the supplied object. For example, each {@link ICompilationUnit} corresponds to at
	 * most a single {@link IFile}.
	 * @param o the object
	 * @deprecated an extension point will be exposed to provide correspondences
	 * @return objects the correspond to object <code>o</code>
	 */
	public static Object[] corresponding(Object o){
		// TODO expose extension point for correspondence rules
		
		if (o instanceof IJavaElement){
			try {
				IResource corresponding = ((IJavaElement) o).getCorrespondingResource();
				return corresponding != null ? new Object[] { corresponding } : new Object[] {  };
			} catch (JavaModelException e) {
				return new Object[]{};
			}
		}else if (o instanceof IProject){
			return new Object [] {JavaCore.create((IProject)o)}; 
		}else{
			return new Object[]{};
		}
	}
	
	/**
	 * @param listener the listener
	 */
	public static void removeCacheListener(IInvalidationListener listener){
		synchronized(getInstance().cacheListeners){
			getInstance().cacheListeners.remove(listener);
		}
	}
	
	/**
	 * @param listener the listener
	 */
	public static void addCacheListener(IInvalidationListener listener){
		synchronized(getInstance().cacheListeners){
			getInstance().cacheListeners.add(listener);
		}
	}
}
