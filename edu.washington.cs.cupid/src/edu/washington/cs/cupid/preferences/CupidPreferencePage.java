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
package edu.washington.cs.cupid.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import edu.washington.cs.cupid.internal.CupidActivator;

/**
 * The main Cupid preference page.
 * @author Todd Schiller
 */
public final class CupidPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Construct the main Cupid preference page.
	 */
	public CupidPreferencePage() {
		super(GRID);
		setPreferenceStore(CupidActivator.getDefault().getPreferenceStore());
		setDescription("Cupid configuration options");
	}
	
	@Override
	public void createFieldEditors() {
	
		addField(
			new DirectoryFieldEditor(
				PreferenceConstants.P_ARROW_DIR,
				"Cupid Pipeline Directory:",
				getFieldEditorParent()));
		
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_IMPURE,
				"Enable side-effecting capabilities (experimental)",
				getFieldEditorParent()));
	}

	@Override
	public void init(final IWorkbench workbench) {
		// NO OP
	}	
}
