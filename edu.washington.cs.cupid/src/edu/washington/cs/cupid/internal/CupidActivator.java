package edu.washington.cs.cupid.internal;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.jobs.ICupidSchedulingRule;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.standard.Count;
import edu.washington.cs.cupid.standard.Empty;
import edu.washington.cs.cupid.standard.Max;
import edu.washington.cs.cupid.standard.MostFrequent;
import edu.washington.cs.cupid.standard.NonEmpty;
import edu.washington.cs.cupid.types.ITypeAdapter;
import edu.washington.cs.cupid.types.ITypeAdapterPublisher;

/**
 * Activator and life-cycle manager for Cupid.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class CupidActivator extends AbstractUIPlugin {
	
	/**
	 * The Cupid plug-in id.
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid"; //$NON-NLS-1$

	private static final String PUBLISH_ID = "edu.washington.cs.cupid.publish"; //$NON-NLS-1$
	
	private static final String CAPABILITY_ID = "edu.washington.cs.cupid.capability"; //$NON-NLS-1$
	
	private static final String TYPE_ADAPTER_ID = "edu.washington.cs.cupid.type.adapter"; //$NON-NLS-1$
	
	private static final String SCHEDULING_RULE_ID = "edu.washington.cs.cupid.scheduling.rule"; //$NON-NLS-1$
	
	private static CupidActivator plugin;
	
	private CupidSelectionService selectionManager;
	
	private ILog pluginLog;
	
	/**
	 * Construct the activator and life-cycle manager for Cupid.
	 */
	public CupidActivator() {
	
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pluginLog = Platform.getLog(context.getBundle());
	
		registerCapabilityExtensions();
		registerPublisherExtensions();
		registerTypeAdapterExtensions();
		registerSchedulingRuleExtensions();
		
		// register standard capabilities
		@SuppressWarnings("rawtypes")
		List<ICapability> standard = Lists.<ICapability>newArrayList(
				new Count(),
				new Empty(),
				new Max(),
				new MostFrequent(),
				new NonEmpty());
		
		for (ICapability<?, ?> capability : standard) {
			CupidPlatform.getCapabilityRegistry().registerStaticCapability(capability);			
		}
		
//      TWS: disabled while getting pure plug-ins to work
//		for (IProject p :  ResourcesPlugin.getWorkspace().getRoot().getProjects() ){
//			if (!p.isHidden() && !ProjectSynchronizer.isShadowProject(p)){
//				ProjectSynchronizer sync = new ProjectSynchronizer("CUPID", p);
//				sync.init();
//				syncs.put(p, sync);
//				sync.stop();
//			}
//		}
		
		selectionManager = CupidSelectionService.getInstance();
		
		final IWorkbench workbench = PlatformUI.getWorkbench();
		
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			window.getSelectionService().addSelectionListener(selectionManager);
		}
		
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				
				for (IWorkbenchPage page : window.getPages()) {
					for (IViewReference view : page.getViewReferences()) {
						selectionManager.injectListeners(view);
					}
					
					page.addPartListener(selectionManager);
				}
				
				window.addPageListener(new IPageListener() {
					@Override
					public void pageActivated(final IWorkbenchPage page) {
						page.addPartListener(selectionManager);
					}
					@Override
					public void pageClosed(final IWorkbenchPage page) {
						page.removePartListener(selectionManager);
					}
					@Override
					public void pageOpened(final IWorkbenchPage page) {
						page.addPartListener(selectionManager);
					}
				});
			}
		});
		
		// TWS: races with java initialization
//		SearchablePluginsManager manager = PDECore.getDefault().getSearchablePluginsManager();
//		manager.removeAllFromJavaSearch();
//
//		ITargetHandle target = TargetPlatformService.getDefault().getWorkspaceTargetHandle();
//		if (target != null) {
//			AddToJavaSearchJob.synchWithTarget(target.getTargetDefinition());
//		}
	}

	private void registerSchedulingRuleExtensions() {
		IConfigurationElement[] extensions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CupidActivator.SCHEDULING_RULE_ID);

		for (IConfigurationElement extension : extensions) {
			try {
				
				ICupidSchedulingRule<?> rule = ((ICupidSchedulingRule<?>) extension.createExecutableExtension("rule"));
				CapabilityExecutor.getSchedulingRuleRegistry().registerSchedulingRule(rule);
				
			} catch (CoreException ex) {
				logError("Error registering scheduling rules for extension " + extension.getName(), ex);
			}
		}
	}
	
	private void registerTypeAdapterExtensions() {
		IConfigurationElement[] extensions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CupidActivator.TYPE_ADAPTER_ID);

		for (IConfigurationElement extension : extensions) {
			try {
				
				ITypeAdapterPublisher publisher = ((ITypeAdapterPublisher) extension.createExecutableExtension("adapter"));
				
				for (ITypeAdapter<?, ?> adapter : publisher.publish()) {
					TypeManager.getTypeAdapterRegistry().registerAdapter(adapter);
				}
			} catch (CoreException ex) {
				logError("Error registering capabilities for extension " + extension.getName(), ex);
			}
		}
	}
	
	/**
	 * Register static capabilities.
	 */
	private void registerCapabilityExtensions() {
		IConfigurationElement[] extensions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CupidActivator.CAPABILITY_ID);

		for (IConfigurationElement extension : extensions) {
			try {
				CupidPlatform.getCapabilityRegistry().registerStaticCapability((ICapability<?, ?>) extension.createExecutableExtension("class"));
			} catch (CoreException ex) {
				logError("Error registering capabilities for extension " + extension.getName(), ex);
			}
		}
	}
	
	/**
	 * Register static capability publishers.
	 */
	private void registerPublisherExtensions() {
		IConfigurationElement[] extensions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CupidActivator.PUBLISH_ID);

		for (IConfigurationElement extension : extensions) {
			try {
				CupidPlatform.getCapabilityRegistry().registerPublisher((ICapabilityPublisher) extension.createExecutableExtension("class"));
			} catch (CoreException ex) {
				logError("Error publishing capabilities for extension " + extension.getName(), ex);
			}
		}
	}
	
	@Override
	public void stop(final BundleContext context) throws Exception {
		Job.getJobManager().cancel(this); // cancel all jobs created by Cupid
		plugin = null;
		super.stop(context);
	}

	/**
	 * @return the shared instance
	 */
	public static CupidActivator getDefault() {
		return plugin;
	}
		
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Log an error in the plugin's log.
	 * @param msg localized error message
	 * @param e the exception
	 */
	public void logError(final String msg, final Exception e) {
		pluginLog.log(new Status(Status.ERROR, CupidActivator.PLUGIN_ID, Status.ERROR, msg, e));			
	}
	
	/**
	 * Log information in the plugin's log.
	 * @param msg localized information message
	 */
	public void logInformation(final String msg) {
		pluginLog.log(new Status(Status.INFO, CupidActivator.PLUGIN_ID, Status.INFO, msg, null));
	}
	
	/**
	 * Log information in the plugin's log.
	 * @param status the Cupid job status to log
	 */
	public void log(final CupidJobStatus status) {
		pluginLog.log(status);	
	}

	
}
