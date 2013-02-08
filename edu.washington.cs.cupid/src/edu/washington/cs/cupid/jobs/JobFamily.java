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

	private final Object[] tags;
	private final int hash;
	
	/**
	 * Create a family using the specified {@link Object} tags. <i>All objects should use reference equality.</i>
	 * @param tags the family tags
	 */
	public JobFamily(final Object... tags) {
		this.tags = tags;
		
//		Class<?> [] tagTypes = new Class<?>[tags.length];
//		for (int i = 0; i < tags.length; i++){
//			tagTypes[i] = tags[i].getClass();
//		}
//		hash = Arrays.hashCode(tagTypes);
		hash = 42;
	}
		
	@Override
	public String toString() {
		return Integer.toHexString(hashCode());
	}

	@Override
	public int hashCode() {
		return hash;
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
		if (tags.length != other.tags.length) {
			return false;
		} else {
			for (int i = 0; i < tags.length; i++){
				if (tags[i] != other.tags[i]){
					return false;
				}
			}
		}
		
		return true;
	}
}
