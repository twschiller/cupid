package edu.washington.cs.cupid.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.washington.cs.cupid.internal.CupidActivator;

public class AdvancedPreferencePage 
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

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
					2 /* min (s) */, 15 /* max (s) */, 1, 1));
	}

	@Override
	public void init(IWorkbench workbench) {
	}	
}
