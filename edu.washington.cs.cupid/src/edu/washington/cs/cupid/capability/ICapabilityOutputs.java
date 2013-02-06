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

public interface ICapabilityOutputs {

	Map<ICapability.IOutput<?>, Object> getOutputs();
	<T> T getOutput(ICapability.IOutput<T> output);
	Object getOutput(String name);
	
}
