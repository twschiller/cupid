package edu.washington.cs.cupid.scripting.java.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.washington.cs.cupid.scripting.java.wizards.JavaCapabilityWizard;

public class CreateScriptAction implements IObjectActionDelegate {

		private Shell shell;
		private ISelection selection;
		
		public CreateScriptAction() {
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
			JavaCapabilityWizard wizard = new JavaCapabilityWizard(selection);
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
