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
package edu.washington.cs.cupid.jdt.compiler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class CompilationUnit extends AbstractCapability<IJavaElement, ICompilationUnit> {

	public CompilationUnit(){
		super("Compilation Unit", 
			  "edu.washington.cs.cupid.jdt.compilationunit", 
			  "Get the compilation unit associated with a Java element",
			  IJavaElement.class,
			  ICompilationUnit.class,
			  Flag.PURE, Flag.LOCAL);
	}
	
	@Override
	public CapabilityJob<IJavaElement, ICompilationUnit> getJob(IJavaElement input) {
		return new CapabilityJob<IJavaElement, ICompilationUnit>(this, input){

			@Override
			protected CapabilityStatus<ICompilationUnit> run(IProgressMonitor monitor) {
				IJavaElement cu = input.getAncestor(IJavaElement.COMPILATION_UNIT);
				
				if (cu != null){
					return CapabilityStatus.makeError(new RuntimeException("No associated compilation unit"));
				}else{
					return CapabilityStatus.makeOk((ICompilationUnit)  cu);
				}
			}
			
		};
	}

}
