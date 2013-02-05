package edu.washington.cs.cupid.capability;

import java.util.Map;

public interface ICapabilityOutput {

	Map<ICapability.Output<?>, Object> getOutputs();
	Object getOutput(String name);
	
}
