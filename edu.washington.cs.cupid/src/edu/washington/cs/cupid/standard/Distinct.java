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
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.GenericLinearCapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearJob;

/**
 * A capability that returns the number of elements in a collection.
 * @author Todd Schiller
 * @param <V> element type
 */
public final class Distinct<V> extends GenericLinearCapability<Collection<V>, Set<V>> {

	/**
	 * A capability that returns the number of elements in a collection.
	 */
	public Distinct() {
		super(
				"Distinct", 
				"Returns the distinct elements in a collection",
				Flag.PURE);
	}

	@Override
	public TypeToken<Collection<V>> getInputType() {
		return new TypeToken<Collection<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<Set<V>> getOutputType() {
		return new TypeToken<Set<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public LinearJob<Collection<V>, Set<V>> getJob(final Collection<V> input) {
		return new ImmediateJob<Collection<V>, Set<V>>(this, input, Sets.newHashSet(input));
	}

}
