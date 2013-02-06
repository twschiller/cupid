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

import edu.washington.cs.cupid.capability.linear.AbstractLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public final class CompilationUnit extends AbstractLinearCapability<IJavaElement, ICompilationUnit> {

	public CompilationUnit(){
		super("Compilation Unit", 
			  "edu.washington.cs.cupid.jdt.compilationunit", 
			  "Get the compilation unit associated with a Java element",
			  IJavaElement.class, ICompilationUnit.class,
			  Flag.PURE);
	}
	
	@Override
	public LinearJob<IJavaElement, ICompilationUnit> getJob(final IJavaElement input) {
		return new LinearJob<IJavaElement, ICompilationUnit>(this, input) {

			@Override
			protected LinearStatus<ICompilationUnit> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					IJavaElement cu = input.getAncestor(IJavaElement.COMPILATION_UNIT);
					
					if (cu != null) {
						return LinearStatus.<ICompilationUnit>makeError(new RuntimeException("No associated compilation unit"));
					} else {
						return LinearStatus.makeOk(getCapability(), (ICompilationUnit)  cu);
					}
				} catch (Exception ex) {
					return LinearStatus.<ICompilationUnit>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
