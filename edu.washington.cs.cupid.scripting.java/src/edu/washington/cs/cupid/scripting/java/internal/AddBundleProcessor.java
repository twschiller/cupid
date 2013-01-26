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
package edu.washington.cs.cupid.scripting.java.internal;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

public class AddBundleProcessor implements IQuickFixProcessor{

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		
		try{
			IResource resource = unit.getCorrespondingResource();

			if (resource.getProject() == Activator.getDefault().getCupidProject()){
				switch (problemId){
				case IProblem.ImportNotFound:
				case IProblem.UndefinedType:
					return true;
				default:
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception ex){
			return false;
		}

	}

	private ImportDeclaration parentImport(ASTNode node){
		
		ASTNode current = node;
		do {
			if (current.getNodeType() == ASTNode.IMPORT_DECLARATION){
				return (ImportDeclaration) current;
			}
		} while ( (current = current.getParent()) != null);
		
		return null;
	}
	
	
	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		List<IJavaCompletionProposal> proposals = Lists.newArrayList();
		
		for (IProblemLocation location : locations){
			switch (location.getProblemId()){
			case IProblem.ImportNotFound:
				ImportDeclaration dec = parentImport(context.getCoveringNode());
				
				if (dec != null){
					
					if (!dec.isOnDemand() && !dec.isStatic()){
						
						Name name = ((QualifiedName) dec.getName());
						Bundle bundle = null;
						
						do{
							bundle = Platform.getBundle(name.getFullyQualifiedName());
							name = ((QualifiedName) name).getQualifier();
						}while (name.isQualifiedName() && bundle == null);
						
						if (bundle != null){
							proposals.add(new AddBundleCompletion(context.getCompilationUnit().getJavaProject(), bundle));
						}
					}
				}
				break;
			case IProblem.UndefinedType:
				String type = location.getProblemArguments()[0];
				break;
			default:
				// NO OP
			}
			
		}
		
		return proposals.toArray(new IJavaCompletionProposal[]{});
	}

}
