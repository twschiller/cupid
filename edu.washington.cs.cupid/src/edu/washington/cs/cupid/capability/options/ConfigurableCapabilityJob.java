package edu.washington.cs.cupid.capability.options;

import edu.washington.cs.cupid.capability.CapabilityJob;

public abstract class ConfigurableCapabilityJob<I, V> extends CapabilityJob<I, V> {

	private final Options options;
	
	public ConfigurableCapabilityJob(final IConfigurableCapability<I, V> capability, final I input, final Options options) {
		super(capability, input);
		this.options = options;
	}

	public Options getOptions() {
		return options;
	}
}
