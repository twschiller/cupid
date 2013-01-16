package edu.washington.cs.cupid.conditional.internal;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * A part listener that ignores all events; intended to be sub-classed.
 * @author Todd Schiller
 */
public class NullPartListener implements IPartListener2 {

	@Override
	public void partActivated(final IWorkbenchPartReference partRef) {
		// NO OP
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPartReference partRef) {
		// NO OP
	}

	@Override
	public void partClosed(final IWorkbenchPartReference partRef) {
		// NO OP
	}

	@Override
	public void partDeactivated(final IWorkbenchPartReference partRef) {
		// NO OP
	}

	@Override
	public void partOpened(final IWorkbenchPartReference partRef) {
		// NO OP
	}

	@Override
	public void partHidden(final IWorkbenchPartReference partRef) {
		// NO OP
	}

	@Override
	public void partVisible(final IWorkbenchPartReference partRef) {
		// NO OP
	}

	@Override
	public void partInputChanged(final IWorkbenchPartReference partRef) {
		// NO OP
	}

}
