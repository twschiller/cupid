/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.internal;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * A part listener that ignores all notifications; meant to be subclassed.
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
