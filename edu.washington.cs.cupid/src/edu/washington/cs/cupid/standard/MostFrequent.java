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

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.GenericLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * A capability that computes the most frequent element in a collection.
 * @author Todd Schiller
 * @param <V> the collection element type
 */
public final class MostFrequent<V> extends GenericLinearCapability<List<V>, V> {
	
	// TODO make efficient
	
	/**
	 * A capability that computes the most frequent element in a collection.
	 */
	public MostFrequent() {
		super(
				"Most Frequent Element", 
				"Get the most frequent element in a collection",
				Flag.PURE);
	}
	
	@Override
	public LinearJob<List<V>, V> getJob(final List<V> input) {
		return new LinearJob<List<V>, V> (this, input) {
			@Override
			protected LinearStatus<V> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
					
					Multiset<V> set = HashMultiset.create();
					set.addAll(input);
					for (V val : Multisets.copyHighestCountFirst(set)) {
						return LinearStatus.makeOk(getCapability(), val);
					}
					
					return LinearStatus.<V>makeError(new IllegalArgumentException("Cannot get most frequent element of empty collection"));
				} catch (Exception ex) {
					return LinearStatus.<V>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
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
