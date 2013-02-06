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
package edu.washington.cs.cupid.tests;

import org.eclipse.jdt.core.IMethod;

import edu.washington.cs.cupid.capability.linear.AbstractLinearCapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearJob;

public class MethodCapabilityTest extends AbstractLinearCapability<IMethod, Boolean> {

	public MethodCapabilityTest(){
		super(
				"Method is foo",
				"edu.washington.cs.cupid.tests.methods.foo",
				"true iff the method is called foo",
				IMethod.class,
				Boolean.class,
				Flag.PURE);
	}

	@Override
	public LinearJob getJob(IMethod input) {
		return new ImmediateJob<IMethod, Boolean>(this, input, input.getElementName().equals("foo"));	
	}
}
