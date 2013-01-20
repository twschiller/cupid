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
import org.eclipse.core.runtime.IProgressMonitor;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

/**
 * An identity capability that immediately returns exceptionally. Returns a {@link IResource}
 * so that it can be chained to other capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class ExceptionCapability extends AbstractCapability<IResource, IResource> {

	public ExceptionCapability(){
		super(
				"Exception",
				"edu.washington.cs.cupid.tests.exception",
				"Throws an exception",
				IResource.class,
				IResource.class,
				Flag.PURE, Flag.LOCAL, Flag.TRANSIENT);
	}
	
	@Override
	public CapabilityJob<IResource, IResource> getJob(IResource input) {
		return new CapabilityJob<IResource, IResource>(this, input){
			@Override
			protected CapabilityStatus<IResource> run(IProgressMonitor monitor) {
				monitor.done();
				return CapabilityStatus.makeError(new RuntimeException("An (expected) exception"));
			}
		};
	}
}
