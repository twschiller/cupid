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
package edu.washington.cs.cupid.scripting.java.quickfix;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
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
import com.google.common.collect.Lists;

import edu.washington.cs.cupid.scripting.java.internal.Activator;
import edu.washington.cs.cupid.scripting.java.internal.ClasspathUtil;

/**
 * Process Quick Fix completions for type errors in the Cupid project.
 * @author Todd Schiller
 */
public final class ClasspathProcessor implements IQuickFixProcessor {

	public ClasspathProcessor() {
		// NO OP
	}
	
	@Override
	public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
		try {
			IResource resource = unit.getCorrespondingResource();
			if (resource.getProject() == Activator.getDefault().getCupidProject()) {
				return problemId == IProblem.UndefinedType
						|| problemId == IProblem.UndefinedType
						|| problemId == IProblem.IsClassPathCorrect
						|| problemId == IProblem.MissingTypeInMethod;
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	private ImportDeclaration parentImport(final ASTNode node) {
		ASTNode current = node;
		do {
			if (current.getNodeType() == ASTNode.IMPORT_DECLARATION) {
				return (ImportDeclaration) current;
			}
			current = current.getParent();
		} while (current != null);
		return null;
	}
	
	private List<IJavaCompletionProposal> buildImportNotFoundProposals(final IInvocationContext context, final IProblemLocation location) {
		List<IJavaCompletionProposal> proposals = Lists.newArrayList();
		
		ImportDeclaration dec = parentImport(context.getCoveringNode());
		
		if (dec != null) {
			if (!dec.isOnDemand() && !dec.isStatic()) {
				
				Name name = ((QualifiedName) dec.getName());
				Bundle bundle = null;
				
				// TODO use ClasspathUtil.bundleForClass here?
				do {
					bundle = Platform.getBundle(name.getFullyQualifiedName());
					name = ((QualifiedName) name).getQualifier();
				} while (name.isQualifiedName() && bundle == null);
				
				if (bundle != null) {
					proposals.add(new AddBundleCompletion(context.getCompilationUnit().getJavaProject(), bundle));
				}
			}
		}
		
		return proposals;
	}
	
	private List<IJavaCompletionProposal> buildMissingBundleProposals(final IInvocationContext context, final IProblemLocation location) {
		final List<IJavaCompletionProposal> proposals = Lists.newArrayList();
		
		String className = location.getProblemArguments()[0];
		
		SearchRequestor requestor = new SearchRequestor(){
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				if (match instanceof TypeDeclarationMatch){
					if (match.getElement() instanceof IType){
						IType type = (IType) match.getElement();
						Bundle bundle = null;
						try {
							bundle = ClasspathUtil.bundleForClass(type.getFullyQualifiedName());
						} catch (ClassNotFoundException e) {
							throw new RuntimeException("Internal error finding bundle for class ", e);
						}
						proposals.add(new AddBundleCompletion(context.getCompilationUnit().getJavaProject(), bundle));
					} else {
						throw new RuntimeException("Unexpected match of type " + match.getElement().getClass());
					}			
				}
			}
		};
		
		SearchEngine engine = new SearchEngine();
		try {
			engine.search(
					SearchPattern.createPattern(className, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_FULL_MATCH), // pattern
					new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, 
					SearchEngine.createWorkspaceScope(), //scope, 
					requestor, // searchRequestor
					null // progress monitor
			);
		} catch (CoreException ex) {
			// NO OP
		}

		return proposals;
	}
	
	private List<IJavaCompletionProposal> buildMissingTypeProposals(final IInvocationContext context, final IProblemLocation location) {
		List<IJavaCompletionProposal> proposals = Lists.newArrayList();
		
		String [] args =  location.getProblemArguments();
		String className = args[0];
		// TODO: don't assume the missing type is the return type
		String missing = args[args.length - 1];
	 
		Bundle bundle = null;
		try {
			bundle = ClasspathUtil.bundleForClass(className);
		} catch (ClassNotFoundException e) {
			
			// inner classes are reported incorrectly by Eclipse
			int i = className.lastIndexOf('.');
			className = className.substring(0, i) + "$" + className.substring(i + 1);
			
			try {
				bundle = ClasspathUtil.bundleForClass(className);
			} catch (ClassNotFoundException e1) {
				return Lists.newArrayList();
			}
		}
		
		if (bundle != null) {
			try {
				File jar = ClasspathUtil.jarForClass(bundle, missing);
				URL url = ClasspathUtil.urlForJar(bundle, jar.getName());
				
				if (url.getPath().contains("!/")) {
					// url is internal to bundle jar
					proposals.add(new ExtractBundleJarCompletion(context.getCompilationUnit().getJavaProject(), bundle, jar.getName()));	
				} else {
					throw new UnsupportedOperationException("Unpacked bundles not supported");
				}	
			} catch (Exception ex) {
				// NO OP	
			}
		}
		
		return proposals;
	}
	
	@Override
	public IJavaCompletionProposal[] getCorrections(final IInvocationContext context, final IProblemLocation[] locations) throws CoreException {
		List<IJavaCompletionProposal> proposals = Lists.newArrayList();
		
		for (IProblemLocation location : locations) {
			switch (location.getProblemId()) {
			case IProblem.ImportNotFound:
				proposals.addAll(buildImportNotFoundProposals(context, location));
				break;
			case IProblem.MissingTypeInMethod:
				proposals.addAll(buildMissingTypeProposals(context, location));
				break;
			case IProblem.UndefinedType:
				proposals.addAll(buildMissingBundleProposals(context, location));
				break;
			default:
				// NO OP
			}
		}
		
		return proposals.toArray(new IJavaCompletionProposal[]{});
	}
}
