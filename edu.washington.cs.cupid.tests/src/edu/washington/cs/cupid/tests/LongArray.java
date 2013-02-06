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

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.AbstractLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class LongArray extends AbstractLinearCapability<IResource, Object[]>  {

	public LongArray(){
		super(
				"Long Array",
				"edu.washington.cs.cupid.tests.longarray",
				"Returns a long array",
				TypeToken.of(IResource.class), new TypeToken<Object[]>(){},
				Flag.PURE);
	}
	
	@Override
	public LinearJob getJob(IResource input) {
		
		return new LinearJob(this, input){

			@Override
			protected LinearStatus run(IProgressMonitor monitor) {
				int size = 1500;
				monitor.beginTask("Create long array", size);
				
				List<Integer> result = Lists.newArrayList();
				for (int i = 0; i < size; i++){
					monitor.worked(1);
					result.add(i);
				}
				monitor.done();
				return LinearStatus.makeOk(getCapability(), result.toArray());
			}
			
		};
	}

}
