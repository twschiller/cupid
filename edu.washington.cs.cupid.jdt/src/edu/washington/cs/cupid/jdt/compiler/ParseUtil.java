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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Utility methods for parsing compilation units.
 * @author Todd Schiller
 */
public final class ParseUtil {

	private ParseUtil(){
		
	}
	
	protected static CompilationUnit parse(final ICompilationUnit unit, final IProgressMonitor monitor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(monitor); // TODO does this call done?
	}
}
