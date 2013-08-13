package edu.washington.cs.cupid.chart.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.chart.PieChartView;

public class CreatePieChartAction implements IObjectActionDelegate {

	private ISelection selection;
	
	public CreatePieChartAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// NO OP
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
					PieChartView v = (PieChartView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PieChartView.ID);
					v.setCapability(capability); 
				} catch (PartInitException e) {
					Activator.getDefault().logError("Error creating histogram for capability: " + capability.getName(), e);;
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
