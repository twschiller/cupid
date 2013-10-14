package edu.washington.cs.cupid.chart.internal;

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
import edu.washington.cs.cupid.chart.HistogramView;

public class CreateHistogramAction implements IObjectActionDelegate {

	private ISelection selection;
	private Shell shell;
	
	public CreateHistogramAction() {
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
				
				String msg = "Error creating histogram for capability: " + capability.getName();
				
				try {
					HistogramView h = (HistogramView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HistogramView.ID);
					h.setCapability(capability); 
				} catch (IllegalArgumentException e){
					ErrorDialog.openError(shell, "Error creating histogram", msg,
							new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
					Activator.getDefault().logError(msg, e);	
				} catch (Exception e) {
					ErrorDialog.openError(shell, "Error creating histogram", msg,
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