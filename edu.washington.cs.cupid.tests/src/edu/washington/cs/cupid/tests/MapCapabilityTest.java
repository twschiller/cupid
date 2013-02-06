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

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.AbstractLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * Capability that returns a map from types to methods for a compilation unit
 * @author Todd Schiller
 */
public class MapCapabilityTest extends AbstractLinearCapability<ICompilationUnit, Map<IType, Set<IMethod>>> {

	public MapCapabilityTest(){
		super(
				"Method Map",
				"edu.washington.cs.cupid.tests.maps.methods",
				"Types and methods for a compilation unit",
				new TypeToken<ICompilationUnit>(){},
				new TypeToken<Map<IType,Set<IMethod>>>(){},
				Flag.PURE);
	}

	@Override
	public LinearJob<ICompilationUnit, Map<IType, Set<IMethod>>> getJob(ICompilationUnit input) {
		return new LinearJob<ICompilationUnit, Map<IType, Set<IMethod>>>(this, input){
			@Override
			protected LinearStatus<Map<IType, Set<IMethod>>> run(IProgressMonitor monitor) {
				monitor.done();
				
				Map<IType, Set<IMethod>> result = Maps.newHashMap();

				try{
					for (IType type : ((ICompilationUnit) getInput()).getTypes()){
						Set<IMethod> methods = Sets.newHashSet(type.getMethods());
						result.put(type, methods);
					}
				}catch(JavaModelException ex){
					return LinearStatus.<Map<IType, Set<IMethod>>>makeError(ex);
				}

				return LinearStatus.makeOk(getCapability(), result);
			}
		};
	}
}
