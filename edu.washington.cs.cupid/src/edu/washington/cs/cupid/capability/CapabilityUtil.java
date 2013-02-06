package edu.washington.cs.cupid.capability;

import java.util.Collection;
import java.util.EnumSet;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability.Flag;
import edu.washington.cs.cupid.capability.ICapability.Output;
import edu.washington.cs.cupid.capability.ICapability.Parameter;

public class CapabilityUtil {
	
	public static Object singleOutput(ICapability capability, CapabilityStatus status){
		return status.value().getOutputs().get(singleOutput(capability));
	}
	
	public static <T> ICapabilityOutput singletonOutput(ICapability capability, T value){
		return singletonOutput((Output<T>)singleOutput(capability), value);
	}
	
	public static <T> ICapabilityOutput singletonOutput(Output<T> output, T value){
		ReturnImpl result = new ReturnImpl();
		result.add(output, value);
		return result;
	}
	
	public static <T> ICapabilityInput singleton(ICapability capability, T argument){
		InputImpl input = new InputImpl();
		Parameter<T> parameter = (Parameter<T>)unaryParameter(capability);
		input.add(parameter, argument);
		return input;
	}
	
	public static Output<?> singleOutput(ICapability capability){
		for (Output<?> output : capability.getOutputs()){
			return output;
		}
		throw new IllegalArgumentException("Capability has multiple output");
	}
	
	public static Parameter<?> unaryParameter(ICapability capability){
		for (Parameter<?> param : capability.getParameters()){
			if (!(param.hasDefault() || param.getType().equals(TypeToken.of(Void.class)))){
				return param;
			}
		}
		throw new IllegalArgumentException("Capability is a generator (takes no inputs)");
	}
	
	public static int inputArrity(final ICapability capability){
		int required = 0;
		for (Parameter<?> param : capability.getParameters()){
			if (!(param.hasDefault() || param.getType().equals(TypeToken.of(Void.class)))){
				required++;
			}
		}
		return required;	
	}
	
	public static boolean isGenerator(final ICapability capability){
		return inputArrity(capability) == 0;
	}
	
	public static boolean isUnary(final ICapability capability){
		return inputArrity(capability) == 1;
	}
	
	public static EnumSet<Flag> union(Collection<? extends ICapability> capabilities){
		EnumSet<Flag> flags = EnumSet.of(Flag.PURE);
		for (ICapability capability : capabilities){
			if (!capability.getFlags().contains(Flag.PURE)){
				flags.remove(Flag.PURE);
			}
			
			if (capability.getFlags().contains(Flag.TRANSIENT)){
				flags.add(Flag.TRANSIENT);
			}
		}
		return flags;
	}
	
}
