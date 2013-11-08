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

import java.util.List;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.GenericLinearCapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;

/**
 * A capability that computes the first element in the collection.
 * @author Todd Schiller
 * @param <V> the collection element type
 */
public final class First<V> extends GenericLinearCapability<List<V>, V> {
	
	/**
	 * A capability that computes the most last element in a collection.
	 */
	public First() {
		super(
				"First Element", 
				"Gets the first item in a list",
				Flag.PURE);
	}
	
	@Override
	public ImmediateJob<List<V>, V> getJob(final List<V> input) {
		if (input.isEmpty()){
			return new ImmediateJob<List<V>, V>(this, input, new IllegalArgumentException("Input cannot be empty"));
		}else{
			return new ImmediateJob<List<V>, V>(this, input, input.get(0));
		}
	}

	@Override
	public TypeToken<List<V>> getInputType() {
		return new TypeToken<List<V>>(getClass()) {};
	}

	@Override
	public TypeToken<V> getOutputType() {
		return new TypeToken<V>(getClass()) {};
	}
}
