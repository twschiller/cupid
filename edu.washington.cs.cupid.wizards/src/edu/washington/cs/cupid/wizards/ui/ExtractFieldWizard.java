package edu.washington.cs.cupid.wizards.ui;

import java.io.IOException;

import org.eclipse.jface.wizard.Wizard;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.Getter;

public class ExtractFieldWizard extends Wizard{
	private ExtractFieldPage page;
	
	public ExtractFieldWizard(){
		this("java.lang.Object");
	}
	
	public ExtractFieldWizard(String clazz){
		this.page = new ExtractFieldPage(clazz);
		this.setWindowTitle("New Extraction Capability");
		this.addPage(page);
	}
	
	@Override
	public boolean performFinish() {
		Getter pipe = page.getGetter();
		try {
			Activator.getDefault().getHydrationService().store(pipe);
			CupidPlatform.getCapabilityRegistry().registerStaticCapability(pipe);
			return true;
		} catch (IOException e) {
			// TODO provide feedback to the user
			return false;
		}
	}

}
