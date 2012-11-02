package edu.washington.cs.cupid.capability;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * An Eclipse capability (i.e., service)
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <T> the output type
 */
public abstract class GenericAbstractCapability<I,V> implements ICapability<I,V>{

	private final String name;
	private final String uniqueId;
	private final String description;
	private final EnumSet<Flag> flags;
	
	public static enum Flag {
		PURE, LOCAL, TRANSIENT
	}
	
	public GenericAbstractCapability(
			String name, String uniqueId, String description,
			Flag... flags) {
		
		this.name = name;
		this.uniqueId = uniqueId;
		this.description = description;
	
		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isPure() {
		return flags.contains(Flag.PURE);
	}

	@Override
	public boolean isLocal() {
		return flags.contains(Flag.LOCAL);
	}

	@Override
	public boolean isTransient() {
		return flags.contains(Flag.TRANSIENT);
	}
	
	@Override
	public String toString() {
		return getUniqueId();
	}

	@Override
	public Set<String> getDynamicDependencies() {
		return new HashSet<String>();
	}
}
