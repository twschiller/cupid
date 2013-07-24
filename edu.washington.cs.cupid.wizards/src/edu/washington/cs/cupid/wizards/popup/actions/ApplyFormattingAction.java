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
import edu.washington.cs.cupid.wizards.ui.FormattingRuleWizard;

public class ApplyFormattingAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;
	
	public ApplyFormattingAction() {
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
		
		FormattingRuleWizard wizard = null;
		
		if (selection.isEmpty()) {
			wizard = new FormattingRuleWizard();
		} else if (selection instanceof IStructuredSelection) {
			
			IStructuredSelection values = (IStructuredSelection) selection;
			
			if (values.size() == 1){
				wizard = new FormattingRuleWizard(values.getFirstElement().getClass());
			} else {
				List<Class<?>> classes = Lists.newArrayList();
				for (Object value : values.toList()){
					classes.add(value.getClass());
				}
				
				List<Class<?>> common = TypeManager.commonSuperClass(classes.toArray(new Class[]{}));
				wizard = common.isEmpty() ? new FormattingRuleWizard() : new FormattingRuleWizard(common.get(0));
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
