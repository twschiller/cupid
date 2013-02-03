/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
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
import edu.washington.cs.cupid.capability.options.ConfigurableCapabilityJob;
import edu.washington.cs.cupid.capability.options.Options;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.internal.CupidJobStatus;
import edu.washington.cs.cupid.internal.SchedulingRuleRegistry;
import edu.washington.cs.cupid.jobs.ISchedulingRuleRegistry;
import edu.washington.cs.cupid.jobs.ImmediateJob;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.preferences.PreferenceConstants;

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
	 * <code>true</code> iff job status should be logged.
	 */
	private boolean logJobStatus;
	
	/**
	 * <code>true</code> iff cache status (e.g., hits and misses) should be logged.
	 */
	private boolean logCacheStatus;
	
	/**
	 * Cupid result caches: Input -> { Run Configuration -> Result }.
	 */
	private final Cache<Object, Cache<RunConfiguration, CacheEntry>> resultCaches;
	
	/**
	 * Running jobs: Input -> { Run Configuration -> Capability Job }.
	 */
	@SuppressWarnings("rawtypes")
	private final Table<Object, RunConfiguration, CapabilityJob> running;
	
	/**
	 * Jobs that have been canceled <i>by this executor</i>.
	 */
	@SuppressWarnings("rawtypes")
	private final Set<CapabilityJob> canceling;
	
	private final Set<IInvalidationListener> cacheListeners = Sets.newIdentityHashSet();
	
	/**
	 * Singleton instance.
	 */
	private static CapabilityExecutor instance = null;
	
	/**
	 * Monitor lock for instance creation.
	 */
	private static final Object INSTANCE_MONITOR = new Object();
	
	private final JobResultCacher cacher = new JobResultCacher();
	private final JobLogger logger = new JobLogger();
	private final JobReaper reaper = new JobReaper();
	private final ISchedulingRuleRegistry scheduler = new SchedulingRuleRegistry();

	private static final CapabilityCacheFactory CACHE_FACTORY = new CapabilityCacheFactory();
	
	private CapabilityExecutor() {
		resultCaches = CacheBuilder.newBuilder().build();
		running = HashBasedTable.create();
		canceling = Sets.newIdentityHashSet();
		
		IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
		logJobStatus = preferences.getBoolean(PreferenceConstants.P_JOB_STATUS_LOGGING);
		logCacheStatus = preferences.getBoolean(PreferenceConstants.P_CACHE_STATUS_LOGGING);
		preferences.addPropertyChangeListener(this);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_JOB_STATUS_LOGGING)) {
			logJobStatus = (Boolean) event.getNewValue();
		} else if (event.getProperty().equals(PreferenceConstants.P_CACHE_STATUS_LOGGING)) {
			logCacheStatus = (Boolean) event.getNewValue();
		}	
	}
	
	/**
	 * @return the singleton instance
	 */
	private static CapabilityExecutor getInstance() {
		synchronized (INSTANCE_MONITOR) {
			if (instance == null) {
				instance = new CapabilityExecutor();
				ResourcesPlugin.getWorkspace().addResourceChangeListener(instance);
			}
			return instance;
		}
	}
	
	public static void stop() {
		synchronized (INSTANCE_MONITOR) {
			if (instance != null) {
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(instance);
				instance = null;
			}
		}
	}
	
	/**
	 * Returns the job scheduling rule registry.
	 * @return the job scheduling rule registry.
	 */
	public static ISchedulingRuleRegistry getSchedulingRuleRegistry() {
		return getInstance().scheduler;
	}
	
	/**
	 * Initializes the default capability result cache for an input.
	 * @author Todd Schiller (tws@cs.washington.edu)
	 */
	private static class CapabilityCacheFactory implements Callable<Cache<RunConfiguration, CacheEntry>> {
		@Override
		public Cache<RunConfiguration, CacheEntry> call() throws Exception {
			return CacheBuilder
					.newBuilder()
					.build();
		}
	}
	
	/**
	 * Returns the cached result, or <code>null</code> if the result is not cached.
	 * @param capability the capability
	 * @param input the input
	 * @param <I> input type
	 * @param <T> output type
	 * @return the cached result, or <code>null</code> if the result is not cached
	 */
	@SuppressWarnings("unchecked")
	private <I, T> T getIfPresent(final ICapability<I, T> capability, final I input, final Options options) {
		synchronized (resultCaches) {
			try {
				Cache<RunConfiguration, CacheEntry> cCache = resultCaches.get(input, CACHE_FACTORY);
				
				CacheEntry cached = cCache.getIfPresent(capability);
				
				if (cached == null) {
					return null;
				} else if (TypeManager.isJavaCompatible(capability.getReturnType(), cached.type)) {
					return (T) cached.value;
				} else {
					throw new TypeException(capability.getReturnType(), cached.type);
				}
			} catch (ExecutionException e) {
				CupidActivator.getDefault().log(new CupidJobStatus(capability.getJob(input), Status.WARNING, "error creating capability cache"));
				return null;
			}
		}
	}
	
	/**
	 * Asynchronously execute a capability with default options.
	 * @param capability the capability
	 * @param input the input
	 * @param <I> input type
	 * @param <T> output type
	 * @param family the job family (used for job cancellation)
	 * @param callback the job listener
	 */
	public static <I, T> void asyncExec(final ICapability<I, T> capability, final I input, final Object family, final IJobChangeListener callback) {
		asyncExec(capability, input, Options.DEFAULT, family, callback);
	}
	
	/**
	 * Asynchronously execute a capability.
	 * @param capability the capability
	 * @param input the input
	 * @param options value overrides for options
	 * @param <I> input type
	 * @param <T> output type
	 * @param family the job family (used for job cancellation)
	 * @param callback the job listener
	 */
	public static <I, T> void asyncExec(final ICapability<I, T> capability, final I input, final Options options, final Object family, final IJobChangeListener callback) {
		CapabilityExecutor executor = getInstance();
		
		T cached = executor.getIfPresent(capability, input, options);
		
		CapabilityJob<I, T> job;
		
		synchronized (executor.running) {
			synchronized (executor.canceling) {
				CapabilityJob<I, T> existing = executor.running.get(input, new RunConfiguration(capability, options));

				if (cached != null) { // CACHED
					if (executor.logCacheStatus) {
						
						if (capability.getParameterType().equals(ICapability.UNIT_TOKEN)) {
							CupidActivator.getDefault().log(new CupidJobStatus(capability.getJob(null), Status.INFO, "cache hit"));
						} else {
							CupidActivator.getDefault().log(new CupidJobStatus(capability.getJob(input), Status.INFO, "cache hit"));
						}
					}
					job = new ImmediateJob<I, T>(capability, input, cached);		

				} else if (existing != null && !executor.canceling.contains(existing)) { // ALREADY RUNNING
					// FIXME this allows jobs to be spuriously killed by other requesters
					// TODO the job might finish before we get a change to add the callback?
					job = existing;

					if (executor.logJobStatus) {
						CupidActivator.getDefault().log(new CupidJobStatus(job, Status.INFO, "attaching new listener"));
					}

				} else { // SPAWN NEW JOB	
					
					job = capability.getParameterType().equals(ICapability.UNIT_TOKEN)
							? capability.getJob(null)
							: capability.getJob(input);
					
					if (job == null) {
						job = new ImmediateJob(capability, input, new MalformedCapabilityException(capability, "Capability returned null job"));
					}
					
					if (!capability.isTransient()) {
						job.addJobChangeListener(executor.cacher);
					}
					job.addJobChangeListener(executor.logger);
					job.addJobChangeListener(executor.reaper);
				}
			}
		}
		
		job.addJobChangeListener(callback);
		
		if (family != null) {
			job.addFamily(family);
		}
		
		job.schedule();
	}
	
	/**
	 * Walks a resource delta to invalidate cache lines.
	 * @author Todd Schiller (tws@cs.washington.edu)
	 */
	private class InvalidationVisitor implements IResourceDeltaVisitor {
		private Set<Object> invalidated = Sets.newIdentityHashSet();
		
		@Override
		public boolean visit(final IResourceDelta delta) throws CoreException {
			if (delta.getAffectedChildren().length == 0) {
				IResource resource = delta.getResource();
				if (resource != null && interesting(delta)) {
					
					// invalidate cache lines
					Set<Object> invalidCacheEntries = Sets.newHashSet();
					for (Object input : resultCaches.asMap().keySet()) {
						if (resource.isConflicting(scheduler.getSchedulingRule(input))) {
							invalidCacheEntries.add(input);
						}
					}
					
					if (!invalidCacheEntries.isEmpty()) {
						if (logCacheStatus) {
							CupidActivator.getDefault().logInformation(
								"Invalidating resource " + resource.getFullPath().toPortableString() + " ejects " + invalidCacheEntries.size() + " entries");
						}
						invalidated.addAll(invalidCacheEntries);
						resultCaches.invalidateAll(invalidCacheEntries);
					}
					
					// cancel obsolete jobs
					for (Object input : running.rowKeySet()) {
						if (resource.isConflicting(scheduler.getSchedulingRule(input))) {
							for (CapabilityJob<?, ?> job : running.row(input).values()) {
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
		private boolean interesting(final IResourceDelta delta) {
			return (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.TYPE)) != 0;
		}
	}
	
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getDelta() != null) {
			synchronized (resultCaches) {
				synchronized (running) {
					synchronized (canceling) {
						try {
							InvalidationVisitor v = new InvalidationVisitor();
							event.getDelta().accept(v);
							
							synchronized (cacheListeners) {
								for (IInvalidationListener listener : cacheListeners) {
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
	
	private class JobResultCacher extends NullJobListener {
		@Override
		public void done(final IJobChangeEvent event) {
			CapabilityJob<?, ?> job = (CapabilityJob<?, ?>) event.getJob();
			synchronized (resultCaches) {
				Object value = ((CapabilityStatus<?>) job.getResult()).value();
				
				if (value != null) {
					if (logCacheStatus) {
						CupidActivator.getDefault().log(new CupidJobStatus(job, Status.INFO, "caching result\t" + job.getCapability().hashCode()));
					}
					
					try {
						if (job.getInput() != null) {
							ICapability<?, ?> capability = job.getCapability();
							Options options = (job instanceof ConfigurableCapabilityJob)
									? ((ConfigurableCapabilityJob<?, ?>) job).getOptions()
									: Options.DEFAULT;
							
							resultCaches
								.get(job.getInput(), CACHE_FACTORY)
								.put(new RunConfiguration(capability, options), new CacheEntry(capability.getReturnType(), value));
						}
					} catch (Exception e) {
						CupidActivator.getDefault().logError("Error adding cache result", e);
					}
				}
			}
		}
	}	
	
	private class JobReaper extends NullJobListener {
		@Override
		public void done(final IJobChangeEvent event) {
			synchronized (running) {
				synchronized (canceling) {
					CapabilityStatus<?> result = (CapabilityStatus<?>) event.getResult();
					CapabilityJob<?, ?> job = (CapabilityJob<?, ?>) event.getJob();
					running.remove(job.getInput(), result.value());
					canceling.add(job);
				}
			}
		}
	}
		
	private class JobLogger implements IJobChangeListener {

		private void log(final Job job, final String message) {
			if (logJobStatus) {
				CupidActivator.getDefault().log(new CupidJobStatus(job, Status.INFO, message));
			}
		}
		
		@Override
		public void aboutToRun(final IJobChangeEvent event) {
			log(event.getJob(), "about to run");
		}

		@Override
		public void awake(final IJobChangeEvent event) {
			log(event.getJob(), "awake");
		}

		@Override
		public void done(final IJobChangeEvent event) {
			log(event.getJob(), "done");
		}

		@Override
		public void running(final IJobChangeEvent event) {
			log(event.getJob(), "running");
		}

		@Override
		public void scheduled(final IJobChangeEvent event) {
			log(event.getJob(), "scheduled");
		}

		@Override
		public void sleeping(final IJobChangeEvent event) {
			log(event.getJob(), "sleeping");
		}	
	}
	
	/**
	 * @param listener the listener
	 */
	public static void removeCacheListener(final IInvalidationListener listener) {
		synchronized (getInstance().cacheListeners) {
			getInstance().cacheListeners.remove(listener);
		}
	}
	
	/**
	 * @param listener the listener
	 */
	public static void addCacheListener(final IInvalidationListener listener) {
		synchronized (getInstance().cacheListeners) {
			getInstance().cacheListeners.add(listener);
		}
	}
	
	private final static class RunConfiguration {
		private final ICapability<?, ?> capability;
		private final Options options;
		
		public RunConfiguration(ICapability<?, ?> capability, Options options) {
			this.capability = capability;
			this.options = options;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + capability.hashCode();
			result = prime * result + options.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof RunConfiguration)) {
				return false;
			}
			RunConfiguration other = (RunConfiguration) obj;
			
			if (!capability.equals(other.capability)){
				return false;
			}
			
			if (!options.equals(other.options)) {
				return false;
			}
			return true;
		}
	}
	
	private final static class CacheEntry {
		private final TypeToken<?> type;
		private final Object value;
		
		private CacheEntry(final TypeToken<?> type, final Object value) {
			this.type = type;
			this.value = value;
		}
	}
}
