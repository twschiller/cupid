package edu.washington.cs.cupid.capability.dynamic;

import java.io.Serializable;
import java.util.Collection;

import edu.washington.cs.cupid.capability.ISerializableCapability;

/**
 * A serializable capability.
 * @param <I> input type
 * @param <V> output type
 * @author Todd Schiller
 */
public abstract class AbstractSerializableCapability<I, V> extends AbstractTransientCapability<I, V> implements ISerializableCapability<I, V> {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new serializable capability.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities component capabilities
	 */
	public AbstractSerializableCapability(final String name, final String description, final Collection<Serializable> capabilities) {
		super(name, description, (Collection) capabilities);
	}

}
