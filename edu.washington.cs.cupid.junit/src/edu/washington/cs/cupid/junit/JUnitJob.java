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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import edu.washington.cs.cupid.capability.linear.ILinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;
import edu.washington.cs.cupid.junit.internal.Activator;

/**
 * Run a JUnit launch configuration, and return the test results.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
@SuppressWarnings({ "restriction" })
public class JUnitJob extends LinearJob<IJavaProject, TestRunSession> {

	private static final int TEST_POLLING_TIME_MILLIS = 10;
	private final String configName;
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	
	/**
	 * Construct a job that runs a JUnit launch configuration, and returns the test results.
	 * @param capability the source capability
	 * @param configuration the configuration to run
	 * @param input the project to run
	 */
	public JUnitJob(final ILinearCapability<IJavaProject, TestRunSession> capability, final String configuration, final IJavaProject input) {
		super(capability, input);
		this.configName = configuration;
	}

	@Override
	protected final void canceling() {
		cancelled.set(true);
	}

	@Override
	protected final LinearStatus<TestRunSession> run(final IProgressMonitor monitor) {
		try {
			monitor.beginTask("Run JUnit Configuration", 100);
			
			ILaunchManager launches = DebugPlugin.getDefault().getLaunchManager();
			
			ILaunchConfiguration configuration = null;
			for (ILaunchConfiguration config : launches.getLaunchConfigurations()) {
				if (config.getName().equals(configName)) {
					configuration = config;
				}
			}
			
			if (configuration == null) {
				monitor.done();
				return LinearStatus.<TestRunSession>makeError(new IllegalArgumentException("No launch configuration named " + configName));
			} else {
				ILaunchConfigurationWorkingCopy copy = configuration.getWorkingCopy();
				copy.setAttribute(JUnitLaunchConfigurationConstants.ATTR_NO_DISPLAY, true);
				copy.setAttribute(ILaunchManager.ATTR_PRIVATE, true);
				copy.setAttribute(JUnitMonitor.MONITOR_TEST_PROPERTY, true);

				// Don't build or register the launch, the JUnit monitor will watch it
				ILaunch launch = copy.launch(
						ILaunchManager.RUN_MODE, 
						new SubProgressMonitor(monitor, 100), 
						false /* build */, false /* register */);

				do {
					try {
						Thread.sleep(TEST_POLLING_TIME_MILLIS);
					} catch (InterruptedException e) {
						return LinearStatus.<TestRunSession>makeCancelled();
					}
				} while (!launch.isTerminated());

				return LinearStatus.makeOk(getCapability(), Activator.getDefaultMonitor().forLaunch(launch));
			}
		} catch (Exception ex) {
			return LinearStatus.<TestRunSession>makeError(ex);
		} finally {
			monitor.done();
		}
	}
}
