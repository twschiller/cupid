package edu.washington.cs.cupid.wizards.ui;

import java.io.IOException;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
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
		Getter<?,?> pipe = page.getGetter();
		try {
			Activator.getDefault().getHydrationService().store(pipe);
			CupidPlatform.getCapabilityRegistry().registerStaticCapability(pipe);
			return true;
		} catch (IOException e) {
			ErrorDialog.openError(
					this.getShell(), 
					"Error Creating Capability", 
					"Error creating capability " + pipe.getName(), 
					new Status(Status.ERROR, Activator.PLUGIN_ID, "Error creating capability", e));
			
			return false;
		}
	}

}
