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
package edu.washington.cs.cupid.usage.events;

/**
 * Constants for {@link CupidEvent#getWhat()} and {@link CupidEvent#getKind()}
 * @author Todd Schiller
 */
public final class EventConstants {

	private EventConstants() {
		// NO OP
	}
	
	public static final String ACTION_WHAT = "action";
	
	public static final String CREATE_CAPABILITY_WHAT = "createCapability"; //$NON-NLS-1$
	public static final String ACTIVATE_CAPABILITY_WHAT = "activateCapability"; //$NON-NLS-1$
	
	public static final String SELECTION_CONTEXT_WHAT = "selectContext"; //$NON-NLS-1$
	public static final String LOADED_WHAT = "loaded"; //$NON-NLS-1$
	public static final String CANCELLED_WHAT = "cancel"; //$NON-NLS-1$
	public static final String FINISH_WHAT = "finish"; //$NON-NLS-1$
}
