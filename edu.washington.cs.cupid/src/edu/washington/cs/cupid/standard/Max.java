package edu.washington.cs.cupid.standard;

import java.util.Collection;
import java.util.Collections;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;
import edu.washington.cs.cupid.jobs.ImmediateJob;

/**
 * A capability that computes the maximum element in a collection.
 * @author Todd Schiller
 * @param <V> the collection element type
 */
public final class Max<V extends Comparable<V>> extends GenericAbstractCapability<Collection<V>, V> {
	
	/**
	 *  A capability that computes the maximum element in a collection.
	 */
	public Max() {
		super(
				"Max", 
				"edu.washington.cs.cupid.standard.max",
				"Get the maximum element in a collection",
				Flag.PURE);
	}
	
	@Override
	public CapabilityJob<Collection<V>, V> getJob(final Collection<V> input) {
		return new ImmediateJob<Collection<V>, V>(this, input, (V) Collections.max(input));
	}

	@Override
	public TypeToken<Collection<V>> getParameterType() {
		return new TypeToken<Collection<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<V> getReturnType() {
		return new TypeToken<V>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

}
