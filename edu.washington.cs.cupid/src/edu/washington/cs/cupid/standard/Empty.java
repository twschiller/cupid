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

import java.util.Collection;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.GenericLinearCapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearJob;

/**
 * A capability that indicates whether or not a collection is empty.
 * @author Todd Schiller
 * @param <V> the collection element type
 */
public final class Empty<V> extends GenericLinearCapability<Collection<V>, Boolean> {

	/**
	 * A capability that indicates whether or not a collection is empty.
	 */
	public Empty() {
		super("Is Empty", 
			  "True if the input is empty",
			  Flag.PURE);
	}

	@Override
	public LinearJob<Collection<V>, Boolean> getJob(final Collection<V> input) {
		return new ImmediateJob<Collection<V>, Boolean>(this, input, input.isEmpty());
	}
	
	@Override
	public TypeToken<Collection<V>> getInputType() {
		return new TypeToken<Collection<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<Boolean> getOutputType() {
		return TypeToken.of(Boolean.class);
	}
}
