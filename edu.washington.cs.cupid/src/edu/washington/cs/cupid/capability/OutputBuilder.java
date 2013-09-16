package edu.washington.cs.cupid.capability;

import edu.washington.cs.cupid.capability.ICapability.IOutput;

/**
 * Fluent/safe interface for building output for a capability
 * @author Todd Schiller
 */
public class OutputBuilder {

	private final ICapability capability;
	private final CapabilityOutputs capabilityOutputs;
	
	public OutputBuilder(ICapability capability) {
		super();
		this.capability = capability;
		this.capabilityOutputs = new CapabilityOutputs();
	}
	
	public <T> void add(IOutput<T> output, T value){
		if (capability.getOutputs().contains(output)){
			capabilityOutputs.add(output, value);			
		}else{
			throw new IllegalArgumentException(
					"Capability '" + capability.getName() + "' has no output '" + output.getName() + "' with type '" + output.getType().toString() + "'");
		}
	}
	
	public CapabilityOutputs getOutputs(){
		for (IOutput<?> o : capability.getOutputs()){
			if (!capabilityOutputs.getOutputs().containsKey(o)){
				throw new IllegalStateException("No value supplied for '" + o.getName() + "' for capability '" + capability.getName() + "'");
			}
		}
		return capabilityOutputs;
	}
}
