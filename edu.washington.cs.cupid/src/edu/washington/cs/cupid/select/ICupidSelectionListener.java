package edu.washington.cs.cupid.select;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Interface for objects that react to workbench selections.
 * @see CupidSelectionService
 * @author Todd Schiller
 */
public interface ICupidSelectionListener {

	/**
	 * Triggered when the user makes a selection in the workbench.
	 * @param part the part where the selection occurred
	 * @param selection the selection
	 */
	void selectionChanged(IWorkbenchPart part, ISelection selection);
	
	/**
	 * Triggered when the user makes a selection in the workbench.
	 * @param part the part where the selection occurred
	 * @param data the data that was selected
	 */
	void selectionChanged(IWorkbenchPart part, Object data);
	
	/**
	 * Triggered when the user makes a selection in the workbench.
	 * @param part the part where the selection occurred
	 * @param data the data that was selected
	 */
	void selectionChanged(IWorkbenchPart part, Object [] data);
	
}
