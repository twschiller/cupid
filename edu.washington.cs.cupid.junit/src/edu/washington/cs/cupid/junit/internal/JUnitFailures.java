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

import java.util.Set;

import org.eclipse.jdt.internal.junit.model.TestElement;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.junit.Types;

/**
 * A capability that results the set of failed tests in a test run session.
 * @see {@link TestRunSession}
 * @see {@link TestElement}
 * @author Todd Schiller
 */
@SuppressWarnings("restriction")
public final class JUnitFailures extends LinearCapability<TestRunSession, Set<TestElement>> {

	/**
	 * Construct a capability that results the set of failed tests in a test run session.
	 */
	public JUnitFailures() {
		super("JUnit Failed Tests",
			  "edu.washington.cs.cupid.junit.internal.failed",
			  "Extract failed tests",
			  TypeToken.of(TestRunSession.class), Types.TEST_ELEMENTS,
			  Flag.PURE);
	}
	
	@Override
	public LinearJob<TestRunSession, Set<TestElement>> getJob(final TestRunSession input) {
		return new ImmediateJob<TestRunSession, Set<TestElement>>(this, input, Sets.newHashSet(input.getAllFailedTestElements()));
	}

}
