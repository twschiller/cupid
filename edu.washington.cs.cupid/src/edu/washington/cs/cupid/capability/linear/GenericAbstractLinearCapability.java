package edu.washington.cs.cupid.capability.linear;

import java.util.Collections;
import java.util.Set;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.ICapabilityInput;
import edu.washington.cs.cupid.capability.OutputImpl;
import edu.washington.cs.cupid.capability.ParameterImpl;

public abstract class GenericAbstractLinearCapability<I, V> extends AbstractCapability implements ILinearCapability<I, V> {

	private Parameter<I> input;
	private Output<V> output;
	
	public GenericAbstractLinearCapability(String name, String uniqueId,
			String description, 
			Flag... flags) {
		super(name, uniqueId, description, flags);
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return Collections.<Parameter<?>>singleton(getParameter());
	}

	@Override
	public Set<Output<?>> getOutputs() {
		return Collections.<Output<?>>singleton(getOutput());
	}

	@Override
	public Parameter<I> getParameter() {
		if (input == null){
			input = new ParameterImpl<I>(null, getInputType());
		}
		return input;
	}

	@Override
	public Output<V> getOutput() {
		if (output == null){
			output = new OutputImpl<V>(null, getOutputType());
		}
		return output;
	}

	public abstract TypeToken<I> getInputType();
	
	public abstract TypeToken<V> getOutputType();
	
	@Override
	public final LinearJob getJob(final ICapabilityInput input) {
		Parameter<?> unary = getParameter();
		I arg = (I) input.getArguments().get(unary);
		return getJob(arg);
	}

}
