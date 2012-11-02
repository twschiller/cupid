package edu.washington.cs.cupid.scripting.java.internal;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.capability.ChangeNotifier;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
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
	
	private List<ICapability<?,?>> dynamic = Lists.newArrayList();
	
	private static final ChangeNotifier notifier = new ChangeNotifier();
	
	public static final String CUPID_PROJECT = "Cupid";
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		cupidProject = root.getProject(CUPID_PROJECT);
		
		IProgressMonitor monitor = new NullProgressMonitor();
		
		
		if (!cupidProject.exists()){
			try{
				cupidProject.create(monitor);
				cupidProject.open(monitor);
				JavaProjectManager.populateCupidProject(cupidProject, monitor);
			}catch (CoreException ex){
				logError("Unable to create Cupid project in workspace", ex);
				return;
			}
		}else{
			try{
				cupidProject.open(monitor);
			}catch (CoreException ex){
				logError("Unable to open Cupid project in workspace", ex);
				return;
			}
		}
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new JavaProjectManager(), IResourceChangeEvent.POST_BUILD);
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

		CupidCapabilityLoader.CapabilityResourceLocator finder = new CupidCapabilityLoader.CapabilityResourceLocator();
		try {
			cupidProject.accept(finder);
		} catch (CoreException e) {
			logError("Error finding changed dynamic class files", e);
			return;
		}

		for (ICompilationUnit clazz : finder.getClasses()){		
			loadDynamicCapability(clazz, false);
		}

		notifier.onChange(this);
	}
	
	public void loadDynamicCapability(IJavaElement file, boolean alert){

		String name = file.getElementName().substring(0, file.getElementName().lastIndexOf('.'));

		logInformation("Loading dynamic capability " + name);
		
		CupidCapabilityLoader loader = new CupidCapabilityLoader(Activator.class.getClassLoader());

		Class<?> definition;
		try {
			definition = loader.loadClass(name);
		} catch (ClassNotFoundException e) {
			logError("Error loading dynamic capability " + name, e);
			return;
		}

		if (definition == null){
			logError("Error loading .class file for " + name, null);
			return;
		}

		ICapability<?,?> capability;
		try {
			capability = (ICapability<?,?>) definition.newInstance();
		} catch (Exception e) {
			logError("Error instantiating dynamic capability " + name, e);
			return;
		}

		ICapability<?,?> old = null;

		for (ICapability<?,?> x : dynamic){
			if (x.getUniqueId().equals(capability.getUniqueId())){
				old = x;
				break;
			}
		}
		if (old != null){
			dynamic.remove(old);
		}
		dynamic.add(capability);


		if (alert){
			notifier.onChange(this);
		}
	}
	
	/**
	 * Log an error in the plugin's log
	 * @param msg localized error message
	 * @param e the exception
	 */
	public void logError(String msg, Exception e){
		getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.ERROR, msg, e));
	}
	
	/**
	 * Log information in the plugin's log
	 * @param msg localized information message
	 */
	public void logInformation(String msg){
		getLog().log(new Status(Status.INFO, Activator.PLUGIN_ID, Status.INFO, msg, null));
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
