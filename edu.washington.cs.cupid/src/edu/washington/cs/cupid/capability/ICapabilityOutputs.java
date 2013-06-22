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
package edu.washington.cs.cupid.capability;

import java.util.Map;

/**
 * The outputs produced by a capability.
 * @author Todd Schiller
 */
public interface ICapabilityOutputs {

	/**
	 * @return a mapping from output references to the output value.
	 */
	Map<ICapability.IOutput<?>, Object> getOutputs();
	
	/**
	 * Returns the output value for the given output reference.
	 * @param output the output reference
	 * @return the output value for the given output reference.
	 */
	<T> T getOutput(ICapability.IOutput<T> output);
	
	/**
	 * Returns the output value for the output with the given name.
	 * @param name the name of the output
	 * @return the output value for the output with the given name.
	 */
	Object getOutput(String name);
}
