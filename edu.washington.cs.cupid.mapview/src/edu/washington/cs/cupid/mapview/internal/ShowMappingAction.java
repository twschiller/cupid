package edu.washington.cs.cupid.mapview.internal;


import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.mapview.MapView;

public class ShowMappingAction implements IObjectActionDelegate {

	private ISelection selection;
	private Shell shell;
	
	public ShowMappingAction() {
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
				
				String msg = "Error creating mapping view for capability: " + capability.getName();
				
				try {
					MapView h = (MapView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MapView.ID);
					h.setCapability(capability); 
				} catch (IllegalArgumentException e){
					ErrorDialog.openError(shell, "Error creating mapping view", msg,
							new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
					Activator.getDefault().logError(msg, e);	
				} catch (Exception e) {
					ErrorDialog.openError(shell, "Error creating mapping view", msg,
							new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
					Activator.getDefault().logError(msg, e);
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