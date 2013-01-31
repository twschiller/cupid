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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;

public class AddBundleProcessor implements IQuickFixProcessor{

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		
		try{
			IResource resource = unit.getCorrespondingResource();

			if (resource.getProject() == Activator.getDefault().getCupidProject()){
				switch (problemId){
				case IProblem.ImportNotFound:
				case IProblem.UndefinedType:
				case IProblem.MissingTypeInMethod:
				case IProblem.IsClassPathCorrect:
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
	
	private String innerClassName(String qualifiedName){
		int i = qualifiedName.lastIndexOf('.');
		
		if (i > 0){
			return qualifiedName.substring(0, i) + "$" + qualifiedName.substring(i + 1);
		} else {
			throw new IllegalArgumentException("Name " + qualifiedName + " is not qualified");
		}
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
			case IProblem.MissingTypeInMethod:
				String [] args =  location.getProblemArguments();
				
				String className = args[0];
				String missing = args[args.length-1];
			
				Bundle bundle = null;
				
				try {
					bundle = ClasspathUtil.bundleForClass(className);
				} catch (ClassNotFoundException e) {
					className = innerClassName(className);
					try {
						bundle = ClasspathUtil.bundleForClass(className);
					} catch (ClassNotFoundException e1) {
						break;
					}
				}
				
				if (bundle != null){
					try {
						File jar = ClasspathUtil.jarForClass(bundle, missing);
						URL url = ClasspathUtil.urlForJar(bundle, jar.getName());
						
						if (url.getPath().contains("!/")){
							// url is internal to bundle jar
							proposals.add(new ExtractBundleJarCompletion(context.getCompilationUnit().getJavaProject(), bundle, jar.getName()));
							
						} else {
							throw new RuntimeException("Expected internal jar");
						}
						
					} catch (Exception ex) {
						
						
					}
				}
				
				break;
//			case IProblem.IsClassPathCorrect:
//				String type = location.getProblemArguments()[0];
//				try {
//					CodeSource src = Class.forName(type).getProtectionDomain().getCodeSource();
//					if (src != null){
//						proposals.add(new AddCodeSourceCompletion(context.getCompilationUnit().getJavaProject(), src));
//					}
//				} catch (ClassNotFoundException e) {
//					System.err.println(e);
//					// NO OP
//				}
//				break;
			default:
				// NO OP
			}
			
		}
		
		return proposals.toArray(new IJavaCompletionProposal[]{});
	}

}
