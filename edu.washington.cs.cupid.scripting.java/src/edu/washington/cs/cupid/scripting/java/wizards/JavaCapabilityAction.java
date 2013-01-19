package edu.washington.cs.cupid.scripting.java.wizards;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

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