package edu.washington.cs.cupid.junit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.junit.internal.Activator;

/**
 * Run a JUnit launch configuration, and return the test results.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
@SuppressWarnings({ "restriction" })
public class JUnitJob extends CapabilityJob<IJavaProject, TestRunSession>{
	
	// TODO handle job cancellation

	private final String configuration;
	
	public JUnitJob(ICapability<IJavaProject, TestRunSession> capability, String configuration, IJavaProject input) {
		super(capability, input);
		this.configuration = configuration;
	}
	
	@Override
	protected CapabilityStatus<TestRunSession> run(IProgressMonitor monitor) {
		ILaunchManager launches = DebugPlugin.getDefault().getLaunchManager();

		try {
			for (ILaunchConfiguration config : launches.getLaunchConfigurations()){
				if (config.getName().equals(configuration)){
					
					ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
					copy.setAttribute(JUnitLaunchConfigurationConstants.ATTR_NO_DISPLAY, true);
					copy.setAttribute(ILaunchManager.ATTR_PRIVATE, true);
					copy.setAttribute(JUnitMonitor.MONITOR_TEST, true);
					
					// Don't build or register the launch, the JUnit monitor will watch it
					ILaunch launch = copy.launch(ILaunchManager.RUN_MODE, monitor, false, false);
					
					do{
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// NO OP
						}
					}while (!launch.isTerminated());

					return CapabilityStatus.makeOk(Activator.getDefaultMonitor().forLaunch(launch));
				}
			}
			return CapabilityStatus.makeError(new IllegalArgumentException("No launch configuration named " + configuration));
		} catch (CoreException ex) {
			return CapabilityStatus.makeError(ex);
		}
	}	
}
