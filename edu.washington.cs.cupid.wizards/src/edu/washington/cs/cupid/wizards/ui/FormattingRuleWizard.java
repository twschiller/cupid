package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.jface.wizard.Wizard;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.FormattingRuleManager;
import edu.washington.cs.cupid.wizards.ui.SelectCapabilityPage.SelectListener;

public class FormattingRuleWizard extends Wizard {

	private SelectCapabilityPage select;
	private FormatPage format;	
	
	public FormattingRuleWizard(){
		this(Object.class);
	}
	
	public FormattingRuleWizard(Class<?> clazz){
		this.select = new SelectCapabilityPage(clazz, TypeToken.of(boolean.class));
		this.format = new FormatPage(clazz);
		this.setWindowTitle("Apply Formatting Rule");
		this.addPage(this.select);	
		this.addPage(this.format);
		
		this.select.addSelectListener(new CapabilityListener());
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
		FormattingRule rule = new FormattingRule(format.getFormatName(),
				select.getInputType().getRawType().getName(),
				select.getCapability() != null ? select.getCapability().getName() : null,
				select.getSnippet(),
				format.getFormat(),
				true /*active*/);
	
		FormattingRuleManager.getInstance().addRule(rule);
		
		select.performCleanup();
		
		return true;
	}
	
	private void setDefaultFormatName(TypeToken<?> type){
		Preconditions.checkNotNull(type);
		format.setFormatName(type.getRawType().getSimpleName() + " formatting");
	}
	
	public class CapabilityListener implements SelectListener{
		@Override
		public void onSelectType(TypeToken<?> type) {
			if (!format.hasUserModifiedName() && type != null){
				setDefaultFormatName(type);
			}
		}
	}
}
