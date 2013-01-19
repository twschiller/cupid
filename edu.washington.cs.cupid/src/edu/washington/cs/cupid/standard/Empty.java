package edu.washington.cs.cupid.standard;

import java.util.Collection;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;
import edu.washington.cs.cupid.jobs.ImmediateJob;

/**
 * A capability that indicates whether or not a collection is empty.
 * @author Todd Schiller
 * @param <V> the collection element type
 */
public final class Empty<V> extends GenericAbstractCapability<Collection<V>, Boolean> {

	/**
	 * A capability that indicates whether or not a collection is empty.
	 */
	public Empty() {
		super("Empty", 
			  "edu.washington.cs.cupid.standard.empty",
			  "True if the input is empty",
			  Flag.PURE);
	}

	@Override
	public CapabilityJob<Collection<V>, Boolean> getJob(final Collection<V> input) {
		return new ImmediateJob<Collection<V>, Boolean>(this, input, input.isEmpty());
	}
	
	@Override
	public TypeToken<Collection<V>> getParameterType() {
		return new TypeToken<Collection<V>>(getClass()) {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<Boolean> getReturnType() {
		return TypeToken.of(Boolean.class);
	}
}
