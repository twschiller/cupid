package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.jface.wizard.Wizard;

public class FormattingRuleWizard extends Wizard {

	private SelectCapabilityPage select;
	
	public FormattingRuleWizard(){
		this(Object.class);
	}
	
	public FormattingRuleWizard(Class<?> clazz){
		this.select = new SelectCapabilityPage(clazz);
		this.setWindowTitle("Apply Formatting Rule");
		this.addPage(this.select);
	}
	
	@Override
	public boolean canFinish() {
		return super.canFinish();
	}
	
	@Override
	public boolean isHelpAvailable() {
		return false;
	}
	
	@Override
	public boolean performFinish() {
		return true;
	}

}
