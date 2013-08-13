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
package edu.washington.cs.cupid.junit.internal;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.junit.JUnitJob;

/**
 * A capability that runs a JUnit test configuration on a project.
 * @author Todd Schiller
 */
@SuppressWarnings({ "restriction" })
public final class JUnitCapability extends LinearCapability<IJavaProject, TestRunSession> {

	private final String configuration;
	
	/**
	 * Construct a capability that runs a JUnit test configuration on a project.
	 * @param configuration the configuration to run
	 */
	public JUnitCapability(final String configuration) {
		super("JUnit",
			  "edu.washington.cs.cupid.junit." + configuration,
			  "JUnit test failures",	
			  IJavaProject.class, TestRunSession.class,
			  Flag.PURE);
		
		this.configuration = configuration;
	}
	
	@Override
	public LinearJob<IJavaProject, TestRunSession> getJob(final IJavaProject input) {
		return new JUnitJob(this, configuration, input);
	}
}
