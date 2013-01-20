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

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;
import edu.washington.cs.cupid.jobs.ImmediateJob;

/**
 * A capability that returns the number of elements in a collection.
 * @author Todd Schiller
 * @param <V> element type
 */
public final class Count<V> extends GenericAbstractCapability<Collection<V>, Integer> {

	/**
	 * A capability that returns the number of elements in a collection.
	 */
	public Count() {
		super(
				"Count", 
				"edu.washington.cs.cupid.standard.count",
				"Count the number of elements in a collection",
				Flag.PURE);
	}

	@Override
	public CapabilityJob<Collection<V>, Integer> getJob(final Collection<V> input) {
		return new ImmediateJob<Collection<V>, Integer>(this, input, input.size());
	}

	@Override
	public TypeToken<Collection<V>> getParameterType() {
		return new TypeToken<Collection<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<Integer> getReturnType() {
		return TypeToken.of(Integer.class);
	}
}
