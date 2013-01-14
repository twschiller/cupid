package edu.washington.cs.cupid.capability.dynamic;

import java.io.Serializable;
import java.util.Collection;

import edu.washington.cs.cupid.capability.ISerializableCapability;

/**
 * A pipeline
 * @author Todd Schiller
 */
public abstract class AbstractSerializableCapability<I,V> extends AbstractTransientCapability<I,V> implements ISerializableCapability<I,V>{
	private static final long serialVersionUID = 1L;

	public AbstractSerializableCapability(String name, String description, Collection<Serializable> capabilities){
		super(name, description, (Collection) capabilities);
	}

}
