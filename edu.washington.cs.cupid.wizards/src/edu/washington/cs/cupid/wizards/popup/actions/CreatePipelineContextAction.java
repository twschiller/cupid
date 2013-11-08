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
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.wizards.ui.CreatePipelineWizard;

public class CreatePipelineContextAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;
	
	public CreatePipelineContextAction() {
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
		
		CreatePipelineWizard wizard = null;
		
		if (selection.isEmpty()) {
			wizard = new CreatePipelineWizard();
		} else if (selection instanceof IStructuredSelection) {
			
			IStructuredSelection values = (IStructuredSelection) selection;
			
			if (values.size() == 1){
				wizard = new CreatePipelineWizard(TypeToken.of(values.getFirstElement().getClass()), values.toArray());
			} else {
				List<Class<?>> classes = Lists.newArrayList();
				for (Object value : values.toList()){
					classes.add(value.getClass());
				}
				
				List<Class<?>> common = TypeManager.commonSuperClass(classes.toArray(new Class[]{}));
				if (common.isEmpty()){
					wizard = new CreatePipelineWizard();
				}else{
					wizard = new CreatePipelineWizard(TypeToken.of(common.get(0)), values.toArray());
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
