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

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * The identity capability that returns after {@link #RUNTIME_IN_MINUTES} minutes.
 * Updates the progress monitor every minute.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class LongRunningCapability extends LinearCapability<IResource, IResource>{
	
	public static final int RUNTIME_IN_SECONDS = 300;
	

	public LongRunningCapability(){
		super(
				"Long Running",
				"edu.washington.cs.cupid.tests.longrunning",
				"Runs for a long time",
				IResource.class, IResource.class,
				Flag.PURE);
	}
	
	@Override
	public LinearJob<IResource, IResource> getJob(IResource input) {
		return new LinearJob<IResource, IResource>(this, input){
			@Override
			protected LinearStatus<IResource> run(IProgressMonitor monitor) {
				monitor.beginTask("Long Job", RUNTIME_IN_SECONDS);
				
				for (int i = 0; i < RUNTIME_IN_SECONDS; i++){
					if (monitor.isCanceled()){
						monitor.done();
						return LinearStatus.<IResource>makeCancelled();
					}
					
					try {
						Thread.sleep(1000); // 1 second
					} catch (InterruptedException e) {
						// NO OP
					} 
					monitor.worked(1);
				}
				
				monitor.done();
				return LinearStatus.makeOk(getCapability(), getInput());
			}
		};
	}

}
