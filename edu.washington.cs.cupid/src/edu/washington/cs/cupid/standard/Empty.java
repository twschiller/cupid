package edu.washington.cs.cupid.standard;

import java.util.Collection;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.jobs.ImmediateJob;

@SuppressWarnings("rawtypes")
public class Empty extends AbstractCapability<Collection<?>, Boolean> {

	public Empty(){
		super(
				"Empty", 
				"edu.washington.cs.cupid.standard.empty",
				"True if the input is empty",
				Types.GENERIC_COLLECTION, TypeToken.of(Boolean.class),
				Flag.PURE);
	}

	@Override
	public CapabilityJob<Collection<?>, Boolean> getJob(Collection input) {
		return new ImmediateJob<Collection<?>, Boolean> (this, input, input.isEmpty());
	}
}
