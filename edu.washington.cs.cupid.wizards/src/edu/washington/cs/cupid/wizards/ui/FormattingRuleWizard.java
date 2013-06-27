package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.jface.wizard.Wizard;

import com.google.common.reflect.TypeToken;

public class FormattingRuleWizard extends Wizard {

	private SelectCapabilityPage select;
	private FormatPage format;	
	
	public FormattingRuleWizard(){
		this(Object.class);
	}
	
	public FormattingRuleWizard(Class<?> clazz){
		this.select = new SelectCapabilityPage(clazz, TypeToken.of(boolean.class));
		this.format = new FormatPage();
		this.setWindowTitle("Apply Formatting Rule");
		this.addPage(this.select);	
		this.addPage(this.format);
	}
	
	@Override
	public boolean isHelpAvailable() {
		return false;
	}
	
	@Override
	public boolean performCancel() {
		select.performCleanup();
		return super.performCancel();
	}
	
	@Override
	public boolean performFinish() {
		select.performCleanup();
		
		
		
		return true;
	}
}
