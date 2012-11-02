package edu.washington.cs.cupid.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import edu.washington.cs.cupid.internal.CupidActivator;


public class CupidPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

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
	public void init(IWorkbench workbench) {
	}	
}