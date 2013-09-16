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
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.junit.TestRunListener;

/**
 * Attach JUnit sessions to launches.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
@SuppressWarnings("restriction")
public final class JUnitMonitor {
	
	/**
	 * Launch property indicating that the launch should be monitored.
	 */
	public static final String MONITOR_TEST_PROPERTY = "cupid_monitor";
	
	/**
	 * Debug event listener that attaches JUnit sessions to launches.
	 */
	private static final JUnitLaunchListener LAUNCH_LISTENER = new JUnitLaunchListener();

	/**
	 * JUnit test sessions for launches.
	 */
	private static final Map<ILaunch, TestRunSession> TEST_SESSIONS = Collections.synchronizedMap(new HashMap<ILaunch, TestRunSession>());
	
	/**
	 * Register the JUnit monitor.
	 */
	public void start() {
		DebugPlugin.getDefault().addDebugEventListener(LAUNCH_LISTENER);
	}
	
	/**
	 * Deregister the JUnit monitor.
	 */
	public void stop() {
		DebugPlugin.getDefault().removeDebugEventListener(LAUNCH_LISTENER);
	}
	
	/**
	 * Returns the test session for <code>launch</code>.
	 * @param launch the launch 
	 * @return  the test session for <code>launch</code>.
	 */
	public TestRunSession forLaunch(final ILaunch launch) {
		return TEST_SESSIONS.get(launch);
	}
	
	/**
	 * Attaches JUnit sessions to JUnit launches; source code taken from {@link JUnitModel}.
	 * @author Todd Schiller (tws@cs.washington.edu)
	 * @see JUnitModel
	 */
	private static class JUnitLaunchListener implements IDebugEventSetListener {
		@Override
		public void handleDebugEvents(final DebugEvent[] events) {
			for (DebugEvent event : events) {
				if (event.getKind() == DebugEvent.CREATE && event.getSource() instanceof RuntimeProcess) {
					ILaunch launch = ((RuntimeProcess) event.getSource()).getLaunch();
					
					ILaunchConfiguration config = launch.getLaunchConfiguration();
					if (config == null) {
						return;
					}
					
					try {
						if (!config.getAttribute(MONITOR_TEST_PROPERTY, false)) {
							return;
						}
					} catch (CoreException e1) {
						return;
					}
					
					final IJavaProject javaProject = JUnitLaunchConfigurationConstants.getJavaProject(config);
					if (javaProject == null) {
						return;
					}
					
					// test whether the launch defines the JUnit attributes
					String portStr = launch.getAttribute(JUnitLaunchConfigurationConstants.ATTR_PORT);
					if (portStr == null) {
						return;
					}
					
					try {
						final int port = Integer.parseInt(portStr);
						connectTestRunner(launch, javaProject, port);
					} catch (NumberFormatException e) {
						return;
					}
				}
			}
		}
		
		private void connectTestRunner(final ILaunch launch, final IJavaProject javaProject, final int port) {
			TestRunSession testRunSession = new TestRunSession(launch, javaProject, port);
			TEST_SESSIONS.put(launch, testRunSession);
			
			Object[] listeners = JUnitCorePlugin.getDefault().getNewTestRunListeners().getListeners();
			for (int i = 0; i < listeners.length; i++) {
				((TestRunListener) listeners[i]).sessionLaunched(testRunSession);
			}
		}
	}	
}
