package edu.washington.cs.cupid.capability.linear;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityInput;

public interface ILinearCapability<I, V> extends ICapability {

	Parameter<I> getParameter();
	
	Output<V> getOutput();

	LinearJob getJob(I input);
	
	LinearJob getJob(ICapabilityInput input);
}
