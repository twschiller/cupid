package edu.washington.cs.cupid.scripting.java.internal;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ChangeNotifier;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.scripting.internal.CompilationUnitLocator;
import edu.washington.cs.cupid.scripting.internal.CupidCapabilityLoader;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements ICapabilityPublisher{

	/**
	 *  The plug-in ID
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.scripting.java"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	private static Activator plugin;
	
	private IProject cupidProject = null;
	
	private static List<ICapability<?,?>> dynamic = Lists.newArrayList();
	private static ChangeNotifier notifier;
	
	public static final String CUPID_PROJECT = "Cupid";
	
	private ILog pluginLog;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pluginLog = getLog();
		notifier  = new ChangeNotifier();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		cupidProject = root.getProject(CUPID_PROJECT);
		
		// force cupid to load first?
		CupidPlatform.class.toString();
		
		if (cupidProject.exists()){
			new Job("Open Cupid Project"){

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try{
						cupidProject.open(monitor);
					}catch (CoreException ex){
						logError("Unable to open Cupid project in workspace", ex);
						return new Status(Status.ERROR, Activator.PLUGIN_ID, "Error opening Cupid project", ex);
					}
		
					ResourcesPlugin.getWorkspace().addResourceChangeListener(new JavaProjectManager(), IResourceChangeEvent.POST_BUILD);
					return Status.OK_STATUS;
				}
				
			}.schedule();
		}else{
			new Job("Create Cupid Project"){
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try{
						cupidProject.create(monitor);
						cupidProject.open(monitor);
						JavaProjectManager.populateCupidProject(cupidProject, monitor);
					}catch (Exception ex){
						logError("Unable to create Cupid project in workspace", ex);
						return new Status(Status.ERROR, Activator.PLUGIN_ID, "Error creating Cupid project", ex);
					}
					
					ResourcesPlugin.getWorkspace().addResourceChangeListener(new JavaProjectManager(), IResourceChangeEvent.POST_BUILD);
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}
	
	public IProject getCupidProject(){
		return cupidProject;
	}
	
	public IJavaProject getCupidJavaProject(){
		return JavaCore.create(cupidProject);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Load the custom capabilities from the Cupid workspace project
	 */
	public void loadDynamicCapabilities(){		
		dynamic.clear();

		CompilationUnitLocator finder = new CompilationUnitLocator();
		try {
			cupidProject.accept(finder);
		} catch (CoreException e) {
			logError("Error finding changed dynamic class files", e);
			return;
		}

		for (ICompilationUnit clazz : finder.getCapabilityClasses()){		
			
			logInformation("Loading dynamic capability " + simpleName(clazz));

			try {
				loadDynamicCapability(clazz, false);
			} catch (Exception e) {
				logError("Error loading dynamic capability " + simpleName(clazz), e);
			} catch (Error e){
				// expected when there are compilation errors
			}
		}

		notifier.onChange(this);
	}
	
	private String simpleName(IJavaElement clazz){
		return clazz.getElementName().substring(0, clazz.getElementName().lastIndexOf('.'));
	}
	
	private ICapability<?,?> find(String uniqueId){
		for (ICapability<?,?> capability : dynamic){
			if (capability.getUniqueId().equals(uniqueId)){
				return capability;
			}
		}
		return null;
	}
	
	private void removeCapability(String uniqueId){
		ICapability<?,?> capability = find(uniqueId);
		if (capability != null){
			dynamic.remove(capability);
		}
	}

	/**
	 * Load a dynamic capability defined by <code>element</code>. If <code>notify</code>, alert
	 * listeners that the capability has been load.
	 * @param element
	 * @param notify
	 * @throws Exception
	 * @throws Error iff there are compilation or type resolution errors
	 */
	public void loadDynamicCapability(IJavaElement element, boolean notify) throws Exception, Error{
		
		CupidCapabilityLoader loader = new CupidCapabilityLoader(Activator.class.getClassLoader());

		Class<?> definition = loader.loadClass(simpleName(element));
		ICapability<?,?> capability = (ICapability<?,?>) definition.newInstance();

		removeCapability(capability.getUniqueId());
		
		dynamic.add(capability);

		if (notify){
			notifier.onChange(this);
		}
	}
	
	/**
	 * Log an error in the plugin's log
	 * @param msg localized error message
	 * @param e the exception
	 */
	public void logError(String msg, Throwable e){
		pluginLog.log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR, msg, e));
	}
	
	/**
	 * Log information in the plugin's log
	 * @param msg localized information message
	 */
	public void logInformation(String msg){
		pluginLog.log(new Status(Status.INFO, Activator.PLUGIN_ID, Status.INFO, msg, null));
	}

	@Override
	public synchronized ICapability<?, ?>[] publish() {
		return dynamic.toArray(new ICapability<?,?>[]{});
	}

	@Override
	public synchronized void addChangeListener(ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}

	@Override
	public synchronized void removeChangeListener(ICapabilityChangeListener listener) {
		notifier.removeChangeListener(listener);
	}
	
}
