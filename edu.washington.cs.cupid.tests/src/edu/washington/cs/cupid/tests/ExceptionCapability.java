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

import org.eclipse.core.resources.IResource;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearJob;

/**
 * An identity capability that immediately returns exceptionally. Returns a {@link IResource}
 * so that it can be chained to other capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class ExceptionCapability extends LinearCapability<IResource, IResource> {

	public ExceptionCapability(){
		super(
				"Exception",
				"edu.washington.cs.cupid.tests.exception",
				"Throws an exception",
				IResource.class, IResource.class,
				Flag.PURE, Flag.TRANSIENT);
	}
	
	@Override
	public LinearJob<IResource, IResource> getJob(final IResource input) {
		return new ImmediateJob<IResource,IResource>(this, input, (Throwable) new RuntimeException("An (expected) exception"));
	}
}
