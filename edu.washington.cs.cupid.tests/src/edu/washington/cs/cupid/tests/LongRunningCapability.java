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
 * The identity capability that returns after {@link #RUNTIME_IN_MINUTES} minutes.
 * Updates the progress monitor every minute.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class LongRunningCapability extends AbstractCapability<IResource, IResource>{
	
	public static final int RUNTIME_IN_SECONDS = 300;
	

	public LongRunningCapability(){
		super(
				"Long Running",
				"edu.washington.cs.cupid.tests.longrunning",
				"Runs for a long time",
				IResource.class,
				IResource.class,
				Flag.PURE, Flag.LOCAL);
	}
	
	@Override
	public CapabilityJob<IResource, IResource> getJob(IResource input) {
		return new CapabilityJob<IResource, IResource>(this, input){
			@Override
			protected CapabilityStatus<IResource> run(IProgressMonitor monitor) {
				monitor.beginTask("Long Job", RUNTIME_IN_SECONDS);
				
				for (int i = 0; i < RUNTIME_IN_SECONDS; i++){
					if (monitor.isCanceled()){
						monitor.done();
						return CapabilityStatus.makeCancelled();
					}
					
					try {
						Thread.sleep(1000); // 1 second
					} catch (InterruptedException e) {
						// NO OP
					} 
					monitor.worked(1);
				}
				
				monitor.done();
				return CapabilityStatus.makeOk(this.input);
			}
		};
	}

}
