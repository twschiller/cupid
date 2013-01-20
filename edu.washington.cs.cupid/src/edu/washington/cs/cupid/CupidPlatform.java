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
package edu.washington.cs.cupid;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.washington.cs.cupid.capability.ICapabilityRegistry;
import edu.washington.cs.cupid.internal.CapabilityRegistry;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.preferences.PreferenceConstants;

/**
 * <p>The central class of the Cupid Platform Runtime. This class cannot be instantiated or subclassed by clients; 
 * all functionality is provided by static methods. Features include:</p>
 * 
 * <ul>
 *  <li>the platform registry of available capabilities</li>
 * </ul>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @see {@link CapabilityExecutor} static methods for executing capabilities
 */
public final class CupidPlatform {

	private static final ICapabilityRegistry REGISTRY = new CapabilityRegistry();
	
	private CupidPlatform() {
		// NO OP	
	}
	
	/**
	 * Returns the directory where the Cupid plugin stores pipelines.
	 * @return the directory where the Cupid plugin stores pipelines
	 */
	public static File getPipelineDirectory() {
		IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
		return new File(preferences.getString(PreferenceConstants.P_ARROW_DIR));
	}
	
	/**
	 * Returns the Cupid capability registry.
	 * @return the Cupid capability registry
	 */
	public static ICapabilityRegistry getCapabilityRegistry() {
		return REGISTRY;
	}

}
