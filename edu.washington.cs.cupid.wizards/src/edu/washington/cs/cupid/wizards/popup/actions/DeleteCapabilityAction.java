package edu.washington.cs.cupid.wizards.popup.actions;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.wizards.internal.Activator;

public class DeleteCapabilityAction implements IObjectActionDelegate {

	private ISelection selection;
	private Shell shell;
	
	public DeleteCapabilityAction() {
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
		if (selection instanceof IStructuredSelection){
			IStructuredSelection values = (IStructuredSelection) selection;
			
			if (values.size() == 1){
				ICapability capability = (ICapability) values.getFirstElement();
				
				try {
					Activator.getDefault().deleteCapability(capability);
				} catch (Exception e) {
					String msg = "An error occurred when deleting capability " + capability.getName();
					
					ErrorDialog.openError(shell, "Unable to delete capability", msg,
							new Status(Status.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
					
					Activator.getDefault().logError(msg, e);;
				}
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
