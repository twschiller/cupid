package edu.washington.cs.cupid.capability;

import java.util.Map;

import edu.washington.cs.cupid.capability.ICapability.Output;

public class ReturnImpl implements ICapabilityOutput{

	private Map<Output<?>, Object> outputs;
	private Map<String, Object> named;
	
	public <T> void add(Output<T> output, T value){
		outputs.put(output, value);
		named.put(output.getName(), value);
	}
	
	
	@Override
	public Map<Output<?>, Object> getOutputs() {
		return outputs;
	}

	@Override
	public Object getOutput(String name) {
		return named.get(name);
	}

}
