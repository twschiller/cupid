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

/**
 * Utility methods for working with capabilities.
 * @author Todd Schiller
 */
public class CapabilityUtil {
	
	/**
	 * Returns the output value for the given single-output capability and capability result
	 * @param capability the capability
	 * @param status the capability's result
	 * @return the result of the capability
	 */
	public static Object singleOutputValue(final ICapability capability, final CapabilityStatus status){
		if (status.value() == null) {
			return null;
		} else{
			return status.value().getOutput(singleOutput(capability));	
		}		
	}
	
	/**
	 * Returns the output reference for the given single-output capability.
	 * @param capability the capability
	 * @return the output reference for the given single-output capability.
	 * @throws IllegalArgumentException if the capability has either zero or more than one output
	 */
	public static IOutput<?> singleOutput(ICapability capability){
		if (capability.getOutputs().isEmpty()){
			throw new IllegalArgumentException("Capability has no outputs");
		} else if (capability.getOutputs().size() > 1) {
			throw new IllegalArgumentException("Capability has multiple outputs");
		} else {
			return capability.getOutputs().iterator().next();
		}
	}
	
	/**
	 * Returns <tt>true</tt> iff the capability produces a single output.
	 * @param capability the capability
	 * @return <tt>true</tt> iff the capability produces a single output.
	 */
	public static boolean hasSingleOutput(ICapability capability){
		return capability.getOutputs().size() == 1;
	}
	
	/**
	 * Returns an {@link ICapabilityOutputs} containing the {@code value} for the capability's
	 * only output.
	 * @param capability a single-output capability
	 * @param value the output value
	 * @return a {@link ICapabilityOutputs} containing the {@code value} for the capability's
	 * only output
	 */
	public static <T> ICapabilityOutputs packSingleOutputValue(ICapability capability, T value){
		return packSingleOutputValue((IOutput<T>) singleOutput(capability), value);
	}
	
	/**
	 * Returns an {@link ICapabilityOutputs} containing {@code value} for {@code output}.
	 * @param output the output
	 * @param value the output's value
	 * @return an {@link ICapabilityOutputs} containing {@code value} for {@code output}
	 */
	public static <T> ICapabilityOutputs packSingleOutputValue(IOutput<T> output, T value){
		CapabilityOutputs result = new CapabilityOutputs();
		result.add(output, value);
		return result;
	}
	
	/**
	 * Returns an {@link ICapabilityArguments} containing {@code argument} for the capability's
	 * only input. Returns an empty {@link ICapabilityArguments} if {@code capability} is nullary.
	 * @param capability a single-input capability
	 * @param argument the argument for the parameter, can pass <tt>null</tt> for generators.
	 * @return an {@link ICapabilityArguments} containing {@code argument} for the capability's
	 * only input
	 */
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
	
	/**
	 * Returns the <i>first</i> non-optional parameter for {@code capability}.
	 * @param capability the unary capability
	 * @return the <i>first</i> non-optional parameter for {@code capability}
	 * @throws IllegalArgumentException if the capability is a generator
	 */
	public static IParameter<?> unaryParameter(ICapability capability){
		for (IParameter<?> param : capability.getParameters()){
			if (!(param.hasDefault() || param.getType().equals(TypeToken.of(Void.class)))){
				return param;
			}
		}
		throw new IllegalArgumentException("Capability is a generator (takes no inputs)");
	}
	
	/**
	 * Returns the number of required parameters for {@code capability}.
	 * @param capability the capability.
	 * @return the number of required parameters for {@code capability}.
	 */
	public static int inputArrity(final ICapability capability){
		int required = 0;
		for (IParameter<?> param : capability.getParameters()){
			if (!(param.hasDefault() || param.getType().equals(TypeToken.of(Void.class)))){
				required++;
			}
		}
		return required;	
	}
	
	/**
	 * Returns a list of options (parameters with default values) for {@code capability}.
	 * @param capability the capability
	 * @return a list of options (parameters with default values) for {@code capability}.
	 */
	public static List<IParameter<?>> options(final ICapability capability){
		List<IParameter<?>> result = Lists.newArrayList();
		for (IParameter<?> param : capability.getParameters()){
			if (param.hasDefault()){
				result.add(param);
			}
		}
		return result;
	}
	
	/**
	 * @param capability the capability
	 * @return <tt>true</tt> if the capability has zero or one inputs, and produces exactly one output
	 */
	public static boolean isLinear(final ICapability capability){
		return (isGenerator(capability) || isUnary(capability)) && capability.getOutputs().size() == 1;
	}
	
	/**
	 * Returns <tt>true</tt> if the capability has zero required inputs.
	 * @param capability the capability
	 * @return <tt>true</tt> if the capability has zero required inputs
	 * @see {@link CapabilityUtil#isUnary}
	 */
	public static boolean isGenerator(final ICapability capability){
		return inputArrity(capability) == 0;
	}
	
	/**
	 * Returns <tt>tt</tt> if the capability has exactly one required input
	 * @param capability the capability
	 * @return <tt>tt</tt> if the capability has exactly one required input
	 * @see {@link CapabilityUtil#isGenerator}
	 */
	public static boolean isUnary(final ICapability capability){
		return inputArrity(capability) == 1;
	}
	
	/**
	 * Returns a list of empty argument collections of length {@code length}.
	 * @param length the number empty argument collections to generate.
	 * @return a list of empty argument collections of length {@code length}.
	 */
	public static List<ICapabilityArguments> noArgs(int length){
		List<ICapabilityArguments> result = Lists.newArrayList();
		for (int i = 0; i < length; i++){
			result.add(CapabilityArguments.NONE);
		}
		return result;
	}
	
	/**
	 * Returns the merged flags for the capabilities. For example, the result contains the
	 * {@link Flag#PURE} flag iff all of the capabilities have the flag.
	 * @param capabilities the capabilities
	 * @return the merged flags for the capabilities.
	 */
	public static EnumSet<Flag> mergeFlags(ICapability... capabilities){
		return mergeFlags(Arrays.asList(capabilities));
	}
	
	/**
	 * Returns the merged flags for the capabilities. For example, the result contains the
	 * {@link Flag#PURE} flag iff all of the capabilities have the flag.
	 * @param capabilities the capabilities
	 * @return the merged flags for the capabilities.
	 */
	public static EnumSet<Flag> mergeFlags(Iterable<? extends ICapability> capabilities){
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
