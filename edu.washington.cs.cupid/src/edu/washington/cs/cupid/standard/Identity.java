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
package edu.washington.cs.cupid.standard;

import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearCapability;

/**
 * A capability that computes the last element in a collection.
 * @author Todd Schiller
 * @param <V> the collection element type
 */
public final class Identity extends LinearCapability<Object, Object> {
	
	/**
	 * A capability that computes the most last element in a collection.
	 */
	public Identity() {
		super(
				"Identity", "Returns the element",
				Object.class, Object.class,
				Flag.PURE);
	}
	
	@Override
	public ImmediateJob<Object, Object> getJob(final Object input) {
		return new ImmediateJob<Object,Object>(this, input, input);
	}
}
