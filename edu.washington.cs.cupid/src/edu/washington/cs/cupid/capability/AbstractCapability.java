package edu.washington.cs.cupid.capability;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.google.common.reflect.TypeToken;

/**
 * An Eclipse capability (i.e., service)
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <T> the output type
 */
public abstract class AbstractCapability<I,V> implements ICapability<I,V>{

	private final String name;
	private final String uniqueId;
	private final String description;
	private final EnumSet<Flag> flags;
	private final TypeToken<I> inputType;
	private final TypeToken<V> outputType;
	
	public static enum Flag {
		PURE, LOCAL, TRANSIENT
	}
	
	public AbstractCapability(
			String name, String uniqueId, String description,
			TypeToken<I> inputType, TypeToken<V> outputType, 
			Flag... flags) {
		
		this.name = name;
		this.uniqueId = uniqueId;
		this.description = description;
		this.inputType = inputType;
		this.outputType = outputType;
	
		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
	}
	
	public AbstractCapability(
			String name, String uniqueId, String description,
			Class<I> inputType, Class<V> outputType, 
			Flag... flags) {
		
		this.name = name;
		this.uniqueId = uniqueId;
		this.description = description;
		this.inputType = TypeToken.of(inputType);
		this.outputType = TypeToken.of(outputType);
	
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
	public TypeToken<I> getParameterType() {
		return inputType;
	}

	@Override
	public TypeToken<V> getReturnType() {
		return outputType;
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
