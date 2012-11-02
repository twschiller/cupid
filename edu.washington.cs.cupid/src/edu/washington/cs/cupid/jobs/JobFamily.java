package edu.washington.cs.cupid.jobs;

import java.util.Arrays;

/**
 * A job family constructed from other families
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class JobFamily {

	// TODO This is going to introduce memory leaks. Perhaps we need to intern, and cleanup objects we hold the last reference to?
	
	private final Object[] tags;
	
	/**
	 * Create a family using the specified {@link Object} tags. <i>All objects should use reference equality.</i>
	 * @param tags
	 */
	public JobFamily(Object... tags){
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
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobFamily other = (JobFamily) obj;
		if (!Arrays.equals(tags, other.tags))
			return false;
		return true;
	}
}
