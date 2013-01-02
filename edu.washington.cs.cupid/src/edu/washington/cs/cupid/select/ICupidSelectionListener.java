package edu.washington.cs.cupid.select;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * @author Todd Schiller
 */
public interface ICupidSelectionListener {

	void selectionChanged(IWorkbenchPart part, ISelection selection);
	void selectionChanged(IWorkbenchPart part, Object data);
	void selectionChanged(IWorkbenchPart part, Object data[]);
	
}
