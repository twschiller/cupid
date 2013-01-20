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
package edu.washington.cs.cupid.jobs;

import java.util.Arrays;

/**
 * A job family constructed from other families.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class JobFamily {

	// TODO This is going to introduce memory leaks. Perhaps we need to intern, and cleanup objects we hold the last reference to?
	
	private final Object[] tags;
	
	/**
	 * Create a family using the specified {@link Object} tags. <i>All objects should use reference equality.</i>
	 * @param tags the family tags
	 */
	public JobFamily(final Object... tags) {
		this.tags = tags;
	}
		
	@Override
	public String toString() {
		return Integer.toHexString(hashCode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(tags);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		JobFamily other = (JobFamily) obj;
		if (!Arrays.equals(tags, other.tags)) {
			return false;
		}
		return true;
	}
}
