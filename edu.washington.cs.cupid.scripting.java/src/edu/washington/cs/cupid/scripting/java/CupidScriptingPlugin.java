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
package edu.washington.cs.cupid.scripting.java;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import edu.washington.cs.cupid.scripting.java.internal.UpdateClasspathJob;

/**
 * Activator for the Cupid Java scripting plug-in.
 * @author Todd Schiller
 */
public final class CupidScriptingPlugin extends AbstractUIPlugin implements ICapabilityPublisher {

	/**
	 *  The plug-in ID for the Cupid Java scripting plug-in.
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.scripting.java"; //$NON-NLS-1$

	private static CupidScriptingPlugin plugin;
	
	private IProject cupidProject = null;
	
	private static List<ICapability> dynamic = Lists.newArrayList();
	private static ChangeNotifier notifier;
	
	private JavaProjectManager projectManager = new JavaProjectManager();
	
	/**
	 * The name of the Cupid script project.
	 */
	public static final String CUPID_PROJECT = "Cupid";
	
	private ILog pluginLog;
	
	/**
	 *  Construct the Cupid Java scripting plug-in.
	 */
	public CupidScriptingPlugin() {
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pluginLog = getLog();
		notifier  = new ChangeNotifier();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		cupidProject = root.getProject(CUPID_PROJECT);
		
		try {
		// force cupid to load first?
			CupidPlatform.class.toString();
		}	catch (Exception ex) {
			logError("Error loading CupidPlatform", ex);
		}
		
		if (cupidProject.exists()) {
			new OpenCupidProjectJob().schedule();
		} else {
			new CreateCupidProjectJob().schedule();
		}
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectManager);
	}
	
	/**
	 * Returns the Cupid script project.
	 * @return the Cupid script project
	 */
	public IProject getCupidProject() {
		return cupidProject;
	}
	
	/**
	 * Returns the Cupid script project.
	 * @return the Cupid script project
	 */
	public IJavaProject getCupidJavaProject() {
		return JavaCore.create(cupidProject);
	}

	/**
	 * @return the shared instance
	 */
	public static CupidScriptingPlugin getDefault() {
		return plugin;
	}
	
	private class CreateCupidProjectJob extends Job {
		public CreateCupidProjectJob() {
			super("Create Cupid Project");
			//super.setRule(cupidProject);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				final int totalWork = 3;
				monitor.beginTask("Create Cupid Project", totalWork);
				
				cupidProject.create(new SubProgressMonitor(monitor, 1));
				cupidProject.open(new SubProgressMonitor(monitor, 1));
				JavaProjectManager.populateCupidProject(cupidProject, new SubProgressMonitor(monitor, 1));
			
				ResourcesPlugin.getWorkspace().addResourceChangeListener(projectManager, IResourceChangeEvent.POST_BUILD);
				return Status.OK_STATUS;
			} catch (Exception ex) {
				logError("Unable to create Cupid project in workspace", ex);
				return new Status(Status.ERROR, CupidScriptingPlugin.PLUGIN_ID, "Error creating Cupid project", ex);
			
			} finally {
				monitor.done();
			}
		}
	}
		
	private class OpenCupidProjectJob extends Job {
		public OpenCupidProjectJob() {
			super("Open Cupid Project");
			super.setRule(cupidProject);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				monitor.beginTask("Open Cupid Project", 100);
				
				cupidProject.open(new SubProgressMonitor(monitor, 40));
				ResourcesPlugin.getWorkspace().addResourceChangeListener(projectManager, IResourceChangeEvent.POST_BUILD);
				new UpdateClasspathJob().schedule();
				new LoadCapabilitiesJob().schedule();
				return Status.OK_STATUS;
				
			} catch (CoreException ex) {
				logError("Unable to open Cupid project in workspace", ex);
				return new Status(Status.ERROR, CupidScriptingPlugin.PLUGIN_ID, "Error opening Cupid project", ex);
			
			} finally {
				monitor.done();
			}
		}
		
	}
	
	private class LoadCapabilitiesJob extends Job{
		public LoadCapabilitiesJob() {
			super("Load Dynamic Capabilities");
			super.setRule(cupidProject);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				SubMonitor progress = SubMonitor.convert(monitor, 100);
				
				dynamic.clear();
				
				CompilationUnitLocator finder = new CompilationUnitLocator();
				try {
					cupidProject.accept(finder);
				} catch (CoreException e) {
					logError("Error finding changed dynamic class files", e);
					return new Status(IStatus.ERROR, CupidScriptingPlugin.PLUGIN_ID, "Error finding changed dynamic class files", e);
				}
				
				progress.setWorkRemaining(70);
				
				Set<ICompilationUnit> classes = finder.getCapabilityClasses();
				
				SubMonitor loopProgress = progress.newChild(70).setWorkRemaining(classes.size());
		          
				for (ICompilationUnit clazz : classes) {		
					try {
						loadDynamicCapability(clazz, false);
					} catch (Exception e) {
						logError("Error loading class from file " + simpleName(clazz), e);
					} catch (Error e) {
						// expected when there are compilation errors
					} finally {
						loopProgress.worked(1);
					}
				}

				return Status.OK_STATUS;
			} finally {
				monitor.done();
			}
		}
	}
	
	private String simpleName(final IJavaElement clazz) {
		return clazz.getElementName().substring(0, clazz.getElementName().lastIndexOf('.'));
	}
	
	private ICapability find(final String name) {
		for (ICapability capability : dynamic) {
			if (capability.getName().equals(name)) {
				return capability;
			}
		}
		return null;
	}
	
	/**
	 * Remove and unregister the capability with the given name
	 * @param name the name of the capability
	 */
	private void removeCapability(final String name) {
		ICapability capability = find(name);
		if (capability != null) {
			dynamic.remove(capability);
			notifier.onCapabilityRemoved(capability);
		}
	}

	/**
	 * Load a class defined by <code>element</code>. If <code>notify</code> and the element is a capability, alert
	 * listeners that the capability has been loaded.
	 * @param element the element to load
	 * @param notify <code>true</code> iff listeners should be alerted
	 * @throws Exception if loading the capability fails
	 * @throws Error if there are compilation or type resolution errors
	 */
	public void loadDynamicCapability(final IJavaElement element, final boolean notify) throws Exception, Error {
		
		CupidCapabilityLoader loader = new CupidCapabilityLoader(CupidScriptingPlugin.class.getClassLoader());

		if (!element.getResource().exists()) return;
		
		Class<?> definition = loader.loadClass(simpleName(element));
		
		if (!ICapability.class.isAssignableFrom(definition)) return;
		
		logInformation("Loaded dynamic capability " + definition.getSimpleName() + " from " + simpleName(element));
		
		ICapability capability = (ICapability) definition.newInstance();

		removeCapability(capability.getName());
		
		dynamic.add(capability);

		if (notify) {
			notifier.onCapabilityAdded(capability);
		}
	}
	
	/**
	 * Log an error in the plugin's log.
	 * @param msg localized error message
	 * @param e the exception
	 */
	public void logError(final String msg,  final Throwable e) {
		pluginLog.log(new Status(Status.ERROR, CupidScriptingPlugin.PLUGIN_ID, Status.ERROR, msg, e));
	}
	
	/**
	 * Log information in the plugin's log.
	 * @param msg localized information message
	 */
	public void logInformation(final String msg) {
		pluginLog.log(new Status(Status.INFO, CupidScriptingPlugin.PLUGIN_ID, Status.INFO, msg, null));
	}

	@Override
	public synchronized ICapability[] publish() {
		return dynamic.toArray(new ICapability[] {});
	}

	@Override
	public synchronized void addChangeListener(final ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}

	@Override
	public synchronized void removeChangeListener(final ICapabilityChangeListener listener) {
		notifier.removeChangeListener(listener);
	}
	
}
