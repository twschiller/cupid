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
package edu.washington.cs.cupid.wizards.popup.actions;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.wizards.ui.ExtractFieldWizard;

public class NewExtractCapabilityAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;
	
	/**
	 * Constructor for Action1.
	 */
	public NewExtractCapabilityAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		ExtractFieldWizard wizard = null;
		
		if (selection.isEmpty()) {
			wizard = new ExtractFieldWizard();
		} else if (selection instanceof IStructuredSelection) {
			
			IStructuredSelection values = (IStructuredSelection) selection;
			
			if (values.size() == 1){
				wizard = new ExtractFieldWizard(values.getFirstElement().getClass());
			} else {
				List<Class<?>> classes = Lists.newArrayList();
				for (Object value : values.toList()){
					classes.add(value.getClass());
				}
				
				List<Class<?>> common = TypeManager.commonSuperClass(classes.toArray(new Class[]{}));
				if (common.isEmpty()){
					wizard = new ExtractFieldWizard();
				}else{
					wizard = new ExtractFieldWizard(common.get(0));
				}
			}
		}
		
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.open();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
