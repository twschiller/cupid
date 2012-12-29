package edu.washington.cs.cupid.standard;

import java.util.Collection;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.jobs.ImmediateJob;

@SuppressWarnings("rawtypes")
public class Count extends AbstractCapability<Collection<?>, Integer> {

	public Count(){
		super(
				"Count", 
				"edu.washington.cs.cupid.standard.count",
				"Count the number of elements in a collection",
				Types.GENERIC_COLLECTION, TypeToken.of(Integer.class),
				Flag.PURE);
	}

	@Override
	public CapabilityJob<Collection<?>, Integer> getJob(Collection input) {
		return new ImmediateJob<Collection<?>, Integer> (this, input, input.size());
	}
}
