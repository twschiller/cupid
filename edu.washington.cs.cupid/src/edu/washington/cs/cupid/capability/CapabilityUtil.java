/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.capability;

import java.util.Collection;
import java.util.EnumSet;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability.Flag;
import edu.washington.cs.cupid.capability.ICapability.Output;
import edu.washington.cs.cupid.capability.ICapability.Parameter;

public class CapabilityUtil {
	
	public static Object singleOutputValue(final ICapability capability, final CapabilityStatus status){
		if (status.value() == null) {
			return null;
		} else{
			return status.value().getOutput(singleOutput(capability));	
		}		
	}
	
	public static Output<?> singleOutput(ICapability capability){
		if (capability.getOutputs().isEmpty()){
			throw new IllegalArgumentException("Capability has no outputs");
		} else if (capability.getOutputs().size() > 1) {
			throw new IllegalArgumentException("Capability has multiple outputs");
		} else {
			return capability.getOutputs().iterator().next();
		}
	}
	
	public static <T> ICapabilityOutput packSingleOutputValue(ICapability capability, T value){
		return packSingleOutputValue((Output<T>)singleOutput(capability), value);
	}
	
	public static <T> ICapabilityOutput packSingleOutputValue(Output<T> output, T value){
		ReturnImpl result = new ReturnImpl();
		result.add(output, value);
		return result;
	}
	
	public static <T> ICapabilityInput packUnaryInput(ICapability capability, T argument){
		InputImpl input = new InputImpl();
		Parameter<T> parameter = (Parameter<T>)unaryParameter(capability);
		input.add(parameter, argument);
		return input;
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
