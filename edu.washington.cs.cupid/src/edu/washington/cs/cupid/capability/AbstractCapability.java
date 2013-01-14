package edu.washington.cs.cupid.capability;

import com.google.common.reflect.TypeToken;

/**
 * An Eclipse capability (i.e., service).
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <V> the output type
 */
public abstract class AbstractCapability<I, V> extends GenericAbstractCapability<I, V> {

	private final TypeToken<I> inputType;
	private final TypeToken<V> outputType;
	
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
		
		super(name, uniqueId, description, flags);
		
		this.inputType = inputType;
		this.outputType = outputType;
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
	public final TypeToken<I> getParameterType() {
		return inputType;
	}

	@Override
	public final TypeToken<V> getReturnType() {
		return outputType;
	}
}
