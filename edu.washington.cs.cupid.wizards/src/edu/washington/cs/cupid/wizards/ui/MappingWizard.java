package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.jface.wizard.Wizard;

public class MappingWizard extends Wizard{
	private MappingPage page = new MappingPage();
	
	public MappingWizard(){
		this.setWindowTitle("New Mapping Capability");
		this.addPage(page);
	}
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
