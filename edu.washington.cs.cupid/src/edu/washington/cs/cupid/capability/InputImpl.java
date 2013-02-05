package edu.washington.cs.cupid.capability;

import java.util.Map;

import edu.washington.cs.cupid.capability.ICapability.Parameter;

public class InputImpl implements ICapabilityInput{

	private Map<Parameter<?>, Object> arguments;
	private Map<String, Object> named;
	
	public <T> void add(Parameter<T> parameter, T argument){
		arguments.put(parameter, argument);
		named.put(parameter.getName(), argument);
	}
	
	@Override
	public Map<Parameter<?>, Object> getArguments() {
		return arguments;
	}

	@Override
	public Object getArgument(String name) {
		return named.get(name);
	}

}
