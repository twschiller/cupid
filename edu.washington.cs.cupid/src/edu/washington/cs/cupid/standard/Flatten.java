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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.GenericLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * A capability that flattens nested collections into a single collection
 * @author Todd Schiller
 * @param <V> element type
 */
public final class Flatten<V> extends GenericLinearCapability<Collection<Collection<V>>, List<V>> {

	/**
	 * A capability that returns the number of elements in a collection.
	 */
	public Flatten() {
		super(
				"Flatten", 
				"Flattens nested collections into a single collection",
				Flag.PURE);
	}

	@Override
	public TypeToken<Collection<Collection<V>>> getInputType() {
		return new TypeToken<Collection<Collection<V>>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<List<V>> getOutputType() {
		return new TypeToken<List<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public LinearJob<Collection<Collection<V>>, List<V>> getJob(final Collection<Collection<V>> input) {
		return new LinearJob<Collection<Collection<V>>, List<V>>(this, input) {
			@Override
			protected LinearStatus<List<V>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), getInput().size());
					List<V> result = Lists.newArrayList();
					
					for (Collection<V> inner : getInput()){
						result.addAll(inner);
						monitor.worked(1);
					}
					
					return LinearStatus.makeOk(getCapability(), result);	
				} catch (Exception ex) {
					return LinearStatus.<List<V>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
