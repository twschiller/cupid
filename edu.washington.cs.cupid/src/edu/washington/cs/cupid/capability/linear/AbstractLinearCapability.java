package edu.washington.cs.cupid.capability.linear;

import java.util.Collections;
import java.util.Set;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.OutputImpl;
import edu.washington.cs.cupid.capability.ParameterImpl;

public abstract class AbstractLinearCapability<I, V> extends GenericAbstractLinearCapability<I, V> implements ILinearCapability<I, V> {
	
	private Parameter<I> input;
	private Output<V> output;
	
	public AbstractLinearCapability(String name, String uniqueId,
			String description, 
			TypeToken<I> inputType, TypeToken<V> outputType,
			Flag... flags) {
		super(name, uniqueId, description, flags);
		
		input = new ParameterImpl<I>(null, inputType);
		output = new OutputImpl<V>(null, outputType);
	}
	
	public AbstractLinearCapability(String name, String uniqueId,
			String description, 
			Class<I> inputType, Class<V> outputType,
			Flag... flags) {
		
		this(name, uniqueId, description, TypeToken.of(inputType), TypeToken.of(outputType), flags);
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return Collections.<Parameter<?>>singleton(input);
	}

	@Override
	public Set<Output<?>> getOutputs() {
		return Collections.<Output<?>>singleton(output);
	}

	@Override
	public TypeToken<I> getInputType() {
		return getParameter().getType();
	}

	@Override
	public TypeToken<V> getOutputType() {
		return getOutput().getType();
	}

	@Override
	public Parameter<I> getParameter() {
		return input;
	}

	@Override
	public Output<V> getOutput() {
		return output;
	}
}
