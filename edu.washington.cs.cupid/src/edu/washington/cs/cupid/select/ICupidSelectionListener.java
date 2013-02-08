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
package edu.washington.cs.cupid.select;

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
