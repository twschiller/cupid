/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.wizards.ui;

import java.io.File;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;
import edu.washington.cs.cupid.wizards.internal.Activator;

/**
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
		
		try {
			DynamicSerializablePipeline pipe = page.createPipeline();
			
			ICapability existing = null;
			try{
				existing = CupidPlatform.getCapabilityRegistry().findCapability(pipe.getName());
			}catch(NoSuchCapabilityException ex){
				// good
			}
			if (existing != null) {
				throw new IllegalArgumentException(String.format("Capability already exists with name '%s'; choose a different name.", pipe.getName()));
			}
				
			File file = Activator.getDefault().getHydrationService().store(pipe);
			Activator.getDefault().registerCapability(pipe, file);
			CupidDataCollector.record(
					CupidEventBuilder.createCapabilityEvent(CreatePipelineWizard.class, pipe, Activator.getDefault())
					.addData("length", Integer.toString(page.getPipelineLength()))
					.create());
     		return true;
		} catch (Exception e) {
			String msg = "Error saving capability: " + e.getMessage();
					
			ErrorDialog.openError(
					this.getShell(), msg,
					"Error saving capability", 
					new Status(Status.ERROR, Activator.PLUGIN_ID, msg, e));
			
			Activator.getDefault().logError(msg, e);
			return false;
		}
	}
}
