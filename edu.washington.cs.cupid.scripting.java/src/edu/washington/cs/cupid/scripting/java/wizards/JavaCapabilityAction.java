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
package edu.washington.cs.cupid.scripting.java.wizards;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.washington.cs.cupid.scripting.java.internal.Activator;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;

/**
 * Opens the pipeline creation dialog.
 * @author Todd Schiller
 * @see IWorkbenchWindowActionDelegate
 */
public final class JavaCapabilityAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	
	/**
	 * The constructor.
	 */
	public JavaCapabilityAction() {
	}

	@Override
	public void run(final IAction action) {
		CupidDataCollector.record(
				CupidEventBuilder.createAction(action, this, Activator.getDefault())
				.create());
		
		JavaCapabilityWizard wizard = new JavaCapabilityWizard();
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.open();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(final IWorkbenchWindow window) {
		// cache window object in order to be able to provide parent shell for the message dialog.
		this.window = window;
	}
}
