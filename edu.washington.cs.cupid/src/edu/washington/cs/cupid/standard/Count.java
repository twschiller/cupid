package edu.washington.cs.cupid.standard;

import java.util.Collection;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;
import edu.washington.cs.cupid.jobs.ImmediateJob;

public class Count<V> extends GenericAbstractCapability<Collection<V>, Integer> {

	public Count(){
		super(
				"Count", 
				"edu.washington.cs.cupid.standard.count",
				"Count the number of elements in a collection",
				Flag.PURE);
	}

	@Override
	public CapabilityJob<Collection<V>, Integer> getJob(Collection<V> input) {
		return new ImmediateJob<Collection<V>, Integer> (this, input, input.size());
	}

	@Override
	public TypeToken<Collection<V>> getParameterType() {
		return new TypeToken<Collection<V>>(getClass()){
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<Integer> getReturnType() {
		return TypeToken.of(Integer.class);
	}
}
