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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability.Flag;
import edu.washington.cs.cupid.capability.ICapability.IOutput;
import edu.washington.cs.cupid.capability.ICapability.IParameter;

public class CapabilityUtil {
	
	public static Object singleOutputValue(final ICapability capability, final CapabilityStatus status){
		if (status.value() == null) {
			return null;
		} else{
			return status.value().getOutput(singleOutput(capability));	
		}		
	}
	
	public static IOutput<?> singleOutput(ICapability capability){
		if (capability.getOutputs().isEmpty()){
			throw new IllegalArgumentException("Capability has no outputs");
		} else if (capability.getOutputs().size() > 1) {
			throw new IllegalArgumentException("Capability has multiple outputs");
		} else {
			return capability.getOutputs().iterator().next();
		}
	}
	
	public static boolean hasSingleOutput(ICapability capability){
		return capability.getOutputs().size() == 1;
	}
	
	public static <T> ICapabilityOutputs packSingleOutputValue(ICapability capability, T value){
		return packSingleOutputValue((IOutput<T>) singleOutput(capability), value);
	}
	
	public static <T> ICapabilityOutputs packSingleOutputValue(IOutput<T> output, T value){
		CapabilityOutputs result = new CapabilityOutputs();
		result.add(output, value);
		return result;
	}
	
	public static <T> ICapabilityArguments packUnaryInput(ICapability capability, T argument){
		CapabilityArguments input = new CapabilityArguments();
		
		if (isGenerator(capability)){
			// NO OP
		} else if (isUnary(capability)) {
			IParameter<T> parameter = (IParameter<T>) unaryParameter(capability);
			input.add(parameter, argument);
		} else {
			throw new IllegalArgumentException("Expected generator or unary capability");
		}
		
		return input;
	}
	
	public static IParameter<?> unaryParameter(ICapability capability){
		for (IParameter<?> param : capability.getParameters()){
			if (!(param.hasDefault() || param.getType().equals(TypeToken.of(Void.class)))){
				return param;
			}
		}
		throw new IllegalArgumentException("Capability is a generator (takes no inputs)");
	}
	
	public static int inputArrity(final ICapability capability){
		int required = 0;
		for (IParameter<?> param : capability.getParameters()){
			if (!(param.hasDefault() || param.getType().equals(TypeToken.of(Void.class)))){
				required++;
			}
		}
		return required;	
	}
	
	public static List<IParameter<?>> options(final ICapability capability){
		List<IParameter<?>> result = Lists.newArrayList();
		for (IParameter<?> param : capability.getParameters()){
			if (param.hasDefault()){
				result.add(param);
			}
		}
		return result;
	}
	
	public static boolean isLinear(final ICapability capability){
		return (isGenerator(capability) || isUnary(capability)) && capability.getOutputs().size() == 1;
	}
	
	public static boolean isGenerator(final ICapability capability){
		return inputArrity(capability) == 0;
	}
	
	public static boolean isUnary(final ICapability capability){
		return inputArrity(capability) == 1;
	}
	
	public static EnumSet<Flag> union(ICapability... capabilities){
		return union(Arrays.asList(capabilities));
	}
	
	public static List<ICapabilityArguments> noArgs(int length){
		List<ICapabilityArguments> result = Lists.newArrayList();
		for (int i = 0; i < length; i++){
			result.add(CapabilityArguments.NONE);
		}
		return result;
	}
	
	public static EnumSet<Flag> union(Iterable<? extends ICapability> capabilities){
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
	
	/**
	 * Compares capabilities by name (ascending).
	 */
	public static final Comparator<ICapability> COMPARE_NAME = new Comparator<ICapability>() {
		@Override
		public int compare(final ICapability lhs, final ICapability rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
	};
	
	/**
	 * Constructs a new list of capabilities sorted by <code>comparator</code>.
	 * @param source the source collection
	 * @param comparator sort comparator
	 * @return a new list of capabilities sorted by <code>comparator</code>.
	 */
	public static List<ICapability> sort(final Collection<ICapability> source, final Comparator<ICapability> comparator) {
		List<ICapability> result = Lists.newArrayList(source);
		Collections.sort(result, comparator);
		return result;
	}
	
}
