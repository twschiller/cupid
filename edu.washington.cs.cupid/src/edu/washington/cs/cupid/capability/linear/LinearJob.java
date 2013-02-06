package edu.washington.cs.cupid.capability.linear;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityUtil;

public abstract class LinearJob<I, V> extends CapabilityJob<ILinearCapability<I, V>> {

	private final I input;
	
	public LinearJob(final ILinearCapability<I, V> capability, final I input) {
		super(capability, CapabilityUtil.singleton(capability, input));
		this.input = input;
	}

	public I getInput(){
		return input;
	}
}
