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

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;
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
				
				CupidDataCollector.record(
						CupidEventBuilder.createCapabilityEvent(MappingWizard.class, pipe, Activator.getDefault()).create());
			}else{
				CapabilityMapping<?,?,?> pipe = page.getCapabilityMapping();
				Activator.getDefault().getHydrationService().store(pipe);
				CupidPlatform.getCapabilityRegistry().registerStaticCapability(pipe);
				
				CupidDataCollector.record(
						CupidEventBuilder.createCapabilityEvent(MappingWizard.class, pipe, Activator.getDefault()).create());
			}
			
			return true;
		} catch (Exception e) {
			String msg = "Error creating mapping capability";
			
			ErrorDialog.openError(
					this.getShell(), 
					"Error Creating Capability", msg,
					new Status(Status.ERROR, Activator.PLUGIN_ID, msg, e));
			
			Activator.getDefault().logError(msg, e);
			return false;
		}
	}

}
