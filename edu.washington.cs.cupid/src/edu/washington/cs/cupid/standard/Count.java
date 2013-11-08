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
 * A capability that returns the number of elements in a collection.
 * @author Todd Schiller
 * @param <V> element type
 */
public final class Count<V> extends GenericLinearCapability<Collection<V>, Integer> {

	/**
	 * A capability that returns the number of elements in a collection.
	 */
	public Count() {
		super(
				"Size/ Count / Number of Elements)", 
				"Returns the number of elements in a collection",
				Flag.PURE);
	}

	@Override
	public TypeToken<Collection<V>> getInputType() {
		return new TypeToken<Collection<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<Integer> getOutputType() {
		return TypeToken.of(Integer.class);
	}

	@Override
	public LinearJob<Collection<V>, Integer> getJob(final Collection<V> input) {
		return new ImmediateJob<Collection<V>, Integer>(this, input, Integer.valueOf(input.size()));
	}

}
