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
package edu.washington.cs.cupid.wizards;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.google.common.collect.Lists;

/**
 * Utility classes for working with types.
 * @author Todd Schiller
 */
public final class TypeUtil {

	private TypeUtil() {
		// NO OP
	}
	
	/**
	 * Search for <code>className</code> in the workspace.
	 * @param className a simple or qualified class name
	 * @return instances of the type
	 * @throws CoreException if an error occurred during the search
	 */
	public static List<IType> fetchTypes(String className) throws CoreException {
		if (className == null){
			throw new NullPointerException("Class name to search for cannot be null");
		}
		
		final List<IType> result = Lists.newArrayList();
		
		SearchRequestor requestor = new SearchRequestor(){
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				if (match instanceof TypeDeclarationMatch){
					if (match.getElement() instanceof IType){
						result.add((IType) match.getElement());
					} else {
						throw new RuntimeException("Unexpected match of type " + match.getElement().getClass());
					}			
				}
			}
		};
		
		SearchEngine engine = new SearchEngine();
		
		engine.search(
					SearchPattern.createPattern(className, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_FULL_MATCH), // pattern
					new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, 
					SearchEngine.createWorkspaceScope(), //scope, 
					requestor, // searchRequestor
					null // progress monitor
		);
		
		return result;
	}
	
	/**
	 * Show a single-selection type search dialog. See {@link JavaUI#createTypeDialog}.
	 * @param shell the parent shell of the dialog to be created
	 * @return the selected type, or <code>null</code> if the dialog was cancelled.
	 * @throws JavaModelException if the selection dialog could not be opened
	 */
	public static IType showTypeDialog(Shell shell) throws JavaModelException{
		SelectionDialog dialog;
		
		dialog = JavaUI.createTypeDialog(
				shell, 
				null, // IRunnableContext
				SearchEngine.createWorkspaceScope(),
				IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
				false // Multiple Selection
		);
		
		dialog.open();
		
		return dialog.getResult() == null ? null : (IType) dialog.getResult()[0];
	}
	
}
