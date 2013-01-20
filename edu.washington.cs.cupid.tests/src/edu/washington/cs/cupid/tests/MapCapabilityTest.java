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

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

/**
 * Capability that returns a map from types to methods for a compilation unit
 * @author Todd Schiller
 */
public class MapCapabilityTest extends AbstractCapability<ICompilationUnit, Map<IType, Set<IMethod>>> {

	public MapCapabilityTest(){
		super(
				"Method Map",
				"edu.washington.cs.cupid.tests.maps.methods",
				"Types and methods for a compilation unit",
				new TypeToken<ICompilationUnit>(){},
				new TypeToken<Map<IType,Set<IMethod>>>(){},
				Flag.PURE, Flag.LOCAL);
	}

	@Override
	public CapabilityJob<ICompilationUnit, Map<IType, Set<IMethod>>> getJob(ICompilationUnit input) {
		return new CapabilityJob<ICompilationUnit, Map<IType, Set<IMethod>>>(this, input){
			@Override
			protected CapabilityStatus<Map<IType, Set<IMethod>>> run(IProgressMonitor monitor) {
				monitor.done();
				
				Map<IType, Set<IMethod>> result = Maps.newHashMap();

				try{
					for (IType type : input.getTypes()){
						Set<IMethod> methods = Sets.newHashSet(type.getMethods());
						result.put(type, methods);
					}
				}catch(JavaModelException ex){
					return CapabilityStatus.makeError(ex);
				}

				return CapabilityStatus.makeOk(result);
			}
		};
	}
}
