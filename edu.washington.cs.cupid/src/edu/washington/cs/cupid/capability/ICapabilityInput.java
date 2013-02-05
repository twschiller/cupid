package edu.washington.cs.cupid.capability;

import java.util.Map;

public interface ICapabilityInput {

	Map<ICapability.Parameter<?>, Object> getArguments();
	Object getArgument(String name);
	
}
