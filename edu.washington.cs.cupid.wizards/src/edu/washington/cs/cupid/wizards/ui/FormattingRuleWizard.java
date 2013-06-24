package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.jface.wizard.Wizard;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.ILinearCapability;
import edu.washington.cs.cupid.wizards.ui.SelectCapabilityPage.SelectListener;

public class FormattingRuleWizard extends Wizard {

	private SelectCapabilityPage select;
	private TransformPage transform;
		
	public FormattingRuleWizard(){
		this(Object.class);
	}
	
	public FormattingRuleWizard(Class<?> clazz){
		this.select = new SelectCapabilityPage(clazz);
		this.transform = new TransformPage(TypeToken.of(boolean.class));
		this.setWindowTitle("Apply Formatting Rule");
		this.addPage(this.select);
		
		select.addSelectListener(new SelectListener(){
			@Override
			public void onSelect(ILinearCapability<?, ?> capability) {
				FormattingRuleWizard.this.transform.setInputType(capability.getOutput().getType());
			}
		});
		
		this.addPage(this.transform);
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
