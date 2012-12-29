package edu.washington.cs.cupid.pipes.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.washington.cs.cupid.pipes.Activator;
import edu.washington.cs.cupid.pipes.views.InformationPipelineDialog;


public class CreatePipelineAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public CreatePipelineAction() {
	}

	@Override
	public void run(IAction action) {
		InformationPipelineDialog dialog = new InformationPipelineDialog(window, Activator.getCapabilities());
		if (dialog.open() == Window.OK){
			
			Activator.getDefault().registerPipeline(dialog.getPipeline());
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}