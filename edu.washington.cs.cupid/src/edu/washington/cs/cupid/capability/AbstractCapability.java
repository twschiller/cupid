package edu.washington.cs.cupid.capability;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.google.common.reflect.TypeToken;

/**
 * An Eclipse capability (i.e., service).
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <V> the output type
 */
public abstract class AbstractCapability<I, V> implements ICapability<I, V> {

	private final String name;
	private final String uniqueId;
	private final String description;
	private final EnumSet<Flag> flags;
	private final TypeToken<I> inputType;
	private final TypeToken<V> outputType;
	
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
	 * @param inputType capability input type
	 * @param outputType capability output type
	 * @param flags capability property flags
	 */
	public AbstractCapability(
			final String name, final String uniqueId, final String description,
			final TypeToken<I> inputType, final TypeToken<V> outputType, 
			final Flag... flags) {
		
		this.name = name;
		this.uniqueId = uniqueId;
		this.description = description;
		this.inputType = inputType;
		this.outputType = outputType;
	
		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
	}
	
	/**
	 * A standard capability.
	 * @param name capability name
	 * @param uniqueId capability unique id
	 * @param description capability description
	 * @param inputType capability input type
	 * @param outputType capability output type
	 * @param flags capability property flags
	 */
	public AbstractCapability(
			final String name, final String uniqueId, final String description,
			final Class<I> inputType, final Class<V> outputType, 
			final Flag... flags) {
		
		this(name, uniqueId, description, TypeToken.of(inputType), TypeToken.of(outputType), flags);
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
	public final TypeToken<I> getParameterType() {
		return inputType;
	}

	@Override
	public final TypeToken<V> getReturnType() {
		return outputType;
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
