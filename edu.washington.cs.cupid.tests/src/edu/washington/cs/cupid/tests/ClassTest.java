package edu.washington.cs.cupid.tests;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.jobs.ImmediateJob;

public class ClassTest extends AbstractCapability<Object, String>  {

	public ClassTest(){
		super(
				"Qualified Name",
				"edu.washington.cs.cupid.tests.class",
				"Returns the qualified name of an object",
				Object.class,
				String.class,
				Flag.PURE, Flag.LOCAL);
	}
	
	@Override
	public CapabilityJob<Object, String> getJob(Object input) {
		return new ImmediateJob<Object,String>(this, input, input.getClass().getName());
	}
}
