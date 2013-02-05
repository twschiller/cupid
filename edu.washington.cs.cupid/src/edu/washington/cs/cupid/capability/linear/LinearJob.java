package edu.washington.cs.cupid.capability.linear;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.ICapability;

public abstract class LinearJob extends CapabilityJob {

	private final Object input;
	
	public LinearJob(final ICapability capability, final Object input) {
		super(capability, null);
		this.input = input;
	}

	public Object getInput(){
		return input;
	}
	
}
