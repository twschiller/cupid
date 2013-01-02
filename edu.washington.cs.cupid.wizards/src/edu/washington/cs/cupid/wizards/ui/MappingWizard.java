package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.CapabilityMapping;
import edu.washington.cs.cupid.wizards.internal.ValueMapping;

public class MappingWizard extends Wizard{
	private MappingPage page;
	
	public MappingWizard(TypeToken<?> keyType){
		this.setWindowTitle("New Mapping Capability");
		page = new MappingPage(keyType);
		this.addPage(page);
	}
	
	public MappingWizard(){
		this.setWindowTitle("New Mapping Capability");
		page = new MappingPage();
		this.addPage(page);
	}
	
	@Override
	public boolean performFinish() {
		try {
			if (page.hasKeyAsType()){
				ValueMapping<?, ?> pipe = page.getValueMapping();
				Activator.getDefault().getHydrationService().store(pipe);
				CupidPlatform.getCapabilityRegistry().registerStaticCapability(pipe);	
			}else{
				CapabilityMapping<?,?,?> pipe = page.getCapabilityMapping();
				Activator.getDefault().getHydrationService().store(pipe);
				CupidPlatform.getCapabilityRegistry().registerStaticCapability(pipe);	
			}
			
			return true;
		} catch (Exception e) {
			ErrorDialog.openError(
					this.getShell(), 
					"Error Creating Capability", 
					"Error creating capability", // TODO add more descriptive error message?
					new Status(Status.ERROR, Activator.PLUGIN_ID, "Error creating capability", e));
			
			return false;
		}
	}

}
