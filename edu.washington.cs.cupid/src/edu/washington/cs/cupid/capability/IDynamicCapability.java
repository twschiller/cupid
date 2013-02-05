package edu.washington.cs.cupid.capability;

import java.util.Set;

public interface IDynamicCapability extends ICapability {

	/**
	 * Returns the unique IDs of the capabilities this capability dynamically depends on.
	 * @return the unique IDs of the capabilities this capability dynamically depends on
	 */
	Set<String> getDynamicDependencies();
	
}
