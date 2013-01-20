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
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class CompilerMessagePredicate extends AbstractCapability<ICompilationUnit, Boolean> {

	public CompilerMessagePredicate(){
		super("Compiler Messages (Predicate)", 
			  "edu.washington.cs.cupid.jdt.messages.boolean", 
			  "Has compiler messages (e.g., warnings and errors)",
			  ICompilationUnit.class,
			  Boolean.class,
			  Flag.PURE, Flag.LOCAL);
	}
	
	@Override
	public CapabilityJob<ICompilationUnit, Boolean> getJob(ICompilationUnit input) {
		return new CapabilityJob<ICompilationUnit, Boolean>(this, input){
			@Override
			protected CapabilityStatus<Boolean> run(IProgressMonitor monitor) {
				try{
					CompilationUnit unit = ParseUtil.parse(input, monitor);	
					return CapabilityStatus.makeOk(unit.getMessages().length > 0);
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}
}
