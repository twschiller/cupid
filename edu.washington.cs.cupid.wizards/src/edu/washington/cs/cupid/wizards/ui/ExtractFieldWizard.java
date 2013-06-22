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

import com.google.common.base.Joiner;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEvent;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.Getter;

public class ExtractFieldWizard extends Wizard{
	private ExtractFieldPage page;
	
	public ExtractFieldWizard(){
		this(Object.class);
	}
	
	public ExtractFieldWizard(Class<?> clazz){
		this.page = new ExtractFieldPage(clazz);
		this.setWindowTitle("Extract Data");
		this.addPage(page);
	}
	
	@Override
	public boolean canFinish() {
		return page.hasSelection();
	}
	
	
	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	@Override
	public boolean performFinish() {
		try {
			Getter<?, ?> pipe = page.getGetter();
			Activator.getDefault().getHydrationService().store(pipe);
			CupidPlatform.getCapabilityRegistry().registerStaticCapability(pipe);
			
			CupidEvent event = CupidEventBuilder
					.createCapabilityEvent(ExtractFieldWizard.class, pipe, Activator.getDefault())
					.addData("getter", Joiner.on('.').join(pipe.getFields()))
					.create();
			
			CupidDataCollector.record(event);
			
			return true;
		} catch (Exception e) {
			String msg = "Error creating extract field capability";
			ErrorDialog.openError(
					this.getShell(), 
					"Error Creating Capability", msg,
					new Status(Status.ERROR, Activator.PLUGIN_ID, msg, e));
			
			Activator.getDefault().logError(msg, e);
			return false;
		}
	}
}
