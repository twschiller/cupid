package edu.washington.cs.cupid.capability;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * An Eclipse capability (i.e., service).
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <V> the output type
 */
public abstract class GenericAbstractCapability<I, V> implements ICapability<I, V> {

	private final String name;
	private final String uniqueId;
	private final String description;
	private final EnumSet<Flag> flags;
	
	/**
	 * Capability property flags.
	 * @author Todd Schiller
	 */
	public static enum Flag {
		/**
		 * The capability does not modify state.
		 */
		PURE, 
		
		/**
		 * The capability does not depend on the input's peers.
		 */
		LOCAL, 
		
		/**
		 * The capability depends on external state.
		 */
		TRANSIENT
	}
	
	/**
	 * A standard capability.
	 * @param name capability name
	 * @param uniqueId capability unique id
	 * @param description capability description
	 * @param flags capability property flags
	 */
	public GenericAbstractCapability(
			final String name, final String uniqueId, final String description,
			final Flag... flags) {
		
		this.name = name;
		this.uniqueId = uniqueId;
		this.description = description;
	
		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
	}

	@Override
	public final String getUniqueId() {
		return uniqueId;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String getDescription() {
		return description;
	}

	@Override
	public final boolean isPure() {
		return flags.contains(Flag.PURE);
	}

	@Override
	public final boolean isLocal() {
		return flags.contains(Flag.LOCAL);
	}

	@Override
	public final boolean isTransient() {
		return flags.contains(Flag.TRANSIENT);
	}
	
	@Override
	public final String toString() {
		return getUniqueId();
	}

	@Override
	public final Set<String> getDynamicDependencies() {
		return new HashSet<String>();
	}
}
