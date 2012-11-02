package edu.washington.cs.cupid.junit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.junit.TestRunListener;

/**
 * Attached JUnit sessions to launches
 * @author Todd Schiller (tws@cs.washington.edu)
 */
@SuppressWarnings("restriction")
public class JUnitMonitor {
	
	/**
	 * Launch property indicating that the launch should be monitored
	 */
	public static String MONITOR_TEST = "cupid_monitor";
	
	/**
	 * Debug event listener that attaches JUnit sessions to launches
	 */
	private static JUnitLaunchListener launchListener = new JUnitLaunchListener();

	/**
	 * JUnit test sessions for launches
	 */
	private final static Map<ILaunch, TestRunSession> sessions = Collections.synchronizedMap(new HashMap<ILaunch, TestRunSession>());
	
	/**
	 * Register the JUnit monitor
	 */
	public void start(){
		DebugPlugin.getDefault().addDebugEventListener(launchListener);
	}
	
	/**
	 * Deregister the JUnit monitor
	 */
	public void stop(){
		DebugPlugin.getDefault().removeDebugEventListener(launchListener);
	}
	
	public TestRunSession forLaunch(ILaunch launch){
		return sessions.get(launch);
	}
	
	/**
	 * Attaches JUnit sessions to JUnit launches; source code taken from {@link JUnitModel}.
	 * @author Todd Schiller (tws@cs.washington.edu)
	 * @see JUnitModel
	 */
	private static class JUnitLaunchListener implements IDebugEventSetListener{
		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent event : events){
				if (event.getKind() == DebugEvent.CREATE && event.getSource() instanceof RuntimeProcess){
					ILaunch launch = ((RuntimeProcess) event.getSource()).getLaunch();
					
					ILaunchConfiguration config = launch.getLaunchConfiguration();
					if (config == null){
						return;
					}
					
					try {
						if (!config.getAttribute(MONITOR_TEST, false)){
							return;
						}
					} catch (CoreException e1) {
						return;
					}
					
					final IJavaProject javaProject = JUnitLaunchConfigurationConstants.getJavaProject(config);
					if (javaProject == null){
						return;
					}
					
					// test whether the launch defines the JUnit attributes
					String portStr = launch.getAttribute(JUnitLaunchConfigurationConstants.ATTR_PORT);
					if (portStr == null)
						return;
					try {
						final int port = Integer.parseInt(portStr);
						connectTestRunner(launch, javaProject, port);
					} catch (NumberFormatException e) {
						return;
					}
				}
			}
		}
		
		private void connectTestRunner(ILaunch launch, IJavaProject javaProject, int port) {
			TestRunSession testRunSession= new TestRunSession(launch, javaProject, port);
			sessions.put(launch, testRunSession);
			
			Object[] listeners= JUnitCorePlugin.getDefault().getNewTestRunListeners().getListeners();
			for (int i= 0; i < listeners.length; i++) {
				((TestRunListener) listeners[i]).sessionLaunched(testRunSession);
			}
		}
	}	
}
