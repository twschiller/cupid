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
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.GenericLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * A capability that computes the maximum element in a collection.
 * @author Todd Schiller
 * @param <V> the collection element type
 */
public final class Max<V extends Comparable<V>> extends GenericLinearCapability<Collection<V>, V> {
	
	/**
	 *  A capability that computes the maximum element in a collection.
	 */
	public Max() {
		super(
				"Maximum Element", 
				"Get the maximum element in a collection",
				Flag.PURE);
	}
	
	@Override
	public LinearJob<Collection<V>, V> getJob(final Collection<V> input) {
		return new LinearJob<Collection<V>, V>(this, input) {
			@Override
			protected LinearStatus<V> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
					return LinearStatus.makeOk(getCapability(), Collections.max(input));	
				} catch (Exception ex) {
					return LinearStatus.<V>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

	@Override
	public TypeToken<Collection<V>> getInputType() {
		return new TypeToken<Collection<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<V> getOutputType() {
		return new TypeToken<V>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}
}
