package edu.washington.cs.cupid.capability;

import java.util.Map;

import edu.washington.cs.cupid.capability.ICapability.Parameter;

public interface ICapabilityInput {

	Map<ICapability.Parameter<?>, Object> getArguments();
	<T> T getArgument(Parameter<T> parameter);
	Object getArgument(String name);
	
}
