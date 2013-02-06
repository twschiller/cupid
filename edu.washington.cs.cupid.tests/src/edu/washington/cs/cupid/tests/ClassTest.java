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

import edu.washington.cs.cupid.capability.linear.AbstractLinearCapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearJob;

public class ClassTest extends AbstractLinearCapability<Object, String>  {

	public ClassTest(){
		super(
				"Qualified Name",
				"edu.washington.cs.cupid.tests.class",
				"Returns the qualified name of an object",
				Object.class, String.class,
				Flag.PURE);
	}
	
	@Override
	public LinearJob getJob(final Object input) {
		return new ImmediateJob<Object,String>(this, input, input.getClass().getName());
	}
}
