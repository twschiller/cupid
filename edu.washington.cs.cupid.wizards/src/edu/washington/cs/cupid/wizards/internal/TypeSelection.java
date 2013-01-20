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
package edu.washington.cs.cupid.wizards.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Methods for allowing users to select types
 * @author Todd Schiller
 */
public class TypeSelection {

	/**
	 * Display a dialog for the user to select a type.
	 * @param shell
	 * @param typeReferenceProject
	 * @return the selected type, or <code>null</code> if the user cancelled the dialog
	 * @throws JavaModelException
	 */
	public static Object showTypeDialog(Shell shell, IProject typeReferenceProject) throws JavaModelException{
		SelectionDialog dialog;

		dialog = JavaUI.createTypeDialog(shell, 
				null, // context
				typeReferenceProject,
				IJavaElementSearchConstants.CONSIDER_TYPES,
				false); // multiple selection

		dialog.open();

		Object result[] = dialog.getResult();
		
		return result == null ? null : result[0];
	}
}
