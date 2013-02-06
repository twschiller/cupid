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

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class LongList extends LinearCapability<IResource, List<Integer>>  {

	public LongList(){
		super(
				"Long List",
				"edu.washington.cs.cupid.tests.longlist",
				"Returns a long list",
				TypeToken.of(IResource.class), new TypeToken<List<Integer>>(){},
				Flag.PURE);
	}
	
	@Override
	public LinearJob<IResource, List<Integer>> getJob(IResource input) {
		
		return new LinearJob<IResource, List<Integer>>(this, input){

			@Override
			protected LinearStatus<List<Integer>> run(final IProgressMonitor monitor) {
				int size = 1500;
				monitor.beginTask("Create long list", size);
				
				List<Integer> result = Lists.newArrayList();
				for (int i = 0; i < size; i++){
					monitor.worked(1);
					result.add(i);
				}
				monitor.done();
				return LinearStatus.makeOk(getCapability(), result);
			}
			
		};
	}

}
