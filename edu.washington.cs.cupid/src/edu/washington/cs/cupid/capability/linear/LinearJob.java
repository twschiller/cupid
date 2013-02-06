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

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityUtil;

public abstract class LinearJob<I, V> extends CapabilityJob<ILinearCapability<I, V>> {

	private final I input;
	
	public LinearJob(final ILinearCapability<I, V> capability, final I input) {
		super(capability, CapabilityUtil.singleton(capability, input));
		this.input = input;
	}

	public I getInput(){
		return input;
	}
}
