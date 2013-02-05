package edu.washington.cs.cupid.capability;

import java.util.Collection;
import java.util.EnumSet;

import edu.washington.cs.cupid.capability.ICapability.Flag;
import edu.washington.cs.cupid.capability.ICapability.Output;
import edu.washington.cs.cupid.capability.ICapability.Parameter;

public class CapabilityUtil {
	
	public static <T> ICapabilityOutput singletonOutput(ICapability capability, T value){
		ReturnImpl output = new ReturnImpl();
		output.add((Output<T>)requiredParameter(capability), value);
		return output;
	}
	
	public static <T> ICapabilityInput singleton(ICapability capability, T argument){
		InputImpl input = new InputImpl();
		input.add((Parameter<T>)requiredParameter(capability), argument);
		return input;
	}
	
	public static Parameter<?> requiredParameter(ICapability capability){
		for (Parameter<?> param : capability.getParameters()){
			if (!param.hasDefault()){
				return param;
			}
		}
		throw new IllegalArgumentException("Capability has multiple required parameter");
	}
	
	public static boolean hasSingleRequiredParameter(ICapability capability){
		int needed = 0;
		for (Parameter<?> param : capability.getParameters()){
			if (!param.hasDefault()){
				needed++;
			}
		}
		return needed > 1;
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
