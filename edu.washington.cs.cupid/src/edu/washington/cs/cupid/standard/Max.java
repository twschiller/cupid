package edu.washington.cs.cupid.standard;

import java.util.Collection;
import java.util.Collections;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.jobs.ImmediateJob;

@SuppressWarnings("rawtypes")
public class Max<V extends Comparable> extends AbstractCapability<Collection<V>, V>{
	
	// TODO does type erasure break this?
	
	public Max(){
		super(
				"Max", 
				"edu.washington.cs.cupid.standard.max",
				"Get the maximum element in a collection",
				new TypeToken<Collection<V>>(){}, new TypeToken<V>(){},
				Flag.PURE);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CapabilityJob<Collection<V>, V> getJob(Collection<V> input) {
		return new ImmediateJob<Collection<V>, V> (this, input, (V) Collections.max(input) );
	}

}
