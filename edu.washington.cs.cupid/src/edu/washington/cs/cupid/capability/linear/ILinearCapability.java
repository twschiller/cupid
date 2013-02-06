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
package edu.washington.cs.cupid.capability.linear;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityInput;

public interface ILinearCapability<I, V> extends ICapability {
	Parameter<I> getParameter();
	
	Output<V> getOutput();

	LinearJob<I, V> getJob(final I input);
	
	LinearJob<I, V> getJob(final ICapabilityInput input);
}
