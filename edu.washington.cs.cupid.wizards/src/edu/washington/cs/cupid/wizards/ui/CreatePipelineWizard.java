package edu.washington.cs.cupid.wizards.ui;

import java.io.IOException;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.dynamic.DynamicLinearPipeline;
import edu.washington.cs.cupid.wizards.internal.Activator;

/**
 * 
 * @author Todd Schiller
 */
public class CreatePipelineWizard extends Wizard{

	private CreatePipelinePage page = new CreatePipelinePage();
	
	public CreatePipelineWizard(){
		this.setWindowTitle("New Cupid Pipeline");
		this.addPage(page);
	}
	
	@Override
	public boolean performFinish() {
		DynamicLinearPipeline pipe = page.createPipeline();
		try {
			Activator.getDefault().getHydrationService().store(pipe);
			CupidPlatform.getCapabilityRegistry().registerStaticCapability(pipe);
			return true;
		} catch (IOException e) {
			ErrorDialog.openError(
					this.getShell(), 
					"Error saving capability", 
					"Error saving capability " + pipe.getName() + ": " + e.getMessage(),
					new Status(Status.ERROR, Activator.PLUGIN_ID, "Error saving capability " + pipe.getName(), e));
			
			return false;
		}
	}
}
