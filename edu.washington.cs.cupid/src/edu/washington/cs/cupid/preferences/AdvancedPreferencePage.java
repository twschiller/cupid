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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.washington.cs.cupid.internal.CupidActivator;

/**
 * Advances Cupid options preference page.
 * @author Todd Schiller
 */
public final class AdvancedPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final int MAXIMUM_CANCELATION_SECONDS = 15;
	private static final int MINIMUM_CANCELATION_SECONDS = 2;

	/**
	 * Construct the Cupid advanced options preference page.
	 */
	public AdvancedPreferencePage() {
		super(GRID);
		setPreferenceStore(CupidActivator.getDefault().getPreferenceStore());
		setDescription("Advanced configuration options");
	}

	@Override
	public void createFieldEditors() {
		addField(
				new BooleanFieldEditor(
					PreferenceConstants.P_JOB_STATUS_LOGGING,
					"Enable job status logging",
					getFieldEditorParent()));
		
		addField(
				new BooleanFieldEditor(
					PreferenceConstants.P_CACHE_STATUS_LOGGING,
					"Enable cache status logging (i.e., hits and ejections)",
					getFieldEditorParent()));
		
		addField(
				new ScaleFieldEditor(
					PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS,
					"Inspector View cancelation threshold:",
					getFieldEditorParent(),
					MINIMUM_CANCELATION_SECONDS, MAXIMUM_CANCELATION_SECONDS, 1, 1));
	}

	@Override
	public void init(final IWorkbench workbench) {
		// NO OP
	}	
}
