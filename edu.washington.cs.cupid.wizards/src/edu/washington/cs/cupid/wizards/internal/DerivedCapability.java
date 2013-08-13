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
package edu.washington.cs.cupid.wizards.internal;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IOutput;
import edu.washington.cs.cupid.capability.OutputSelector;
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;

/**
 * Helper class for combination of Capability + Getter
 * @author Todd Schiller
 */
public class DerivedCapability{
	private final ICapability capability;
	private final IOutput<?> output;
	private final IExtractCapability<?,? > getter;
	
	private static final Set<String> FILTER = Sets.newHashSet( 
			"hashCode" , "getClass" , "toString", "iterator", "listIterator", "toArray");
	
	public DerivedCapability(ICapability capability, IOutput<?> output) {
		this(capability, output, null);
	}
	
	public DerivedCapability(ICapability capability, IOutput<?> output, IExtractCapability<?, ?> getter) {
		this.capability = capability;
		this.output = output;
		this.getter = getter;
	}
	
	public DynamicSerializablePipeline toPipeline(){
		
		List<Serializable> pipeline;
		String name;
		
		if (CapabilityUtil.hasSingleOutput(capability)){
			pipeline = Lists.<Serializable>newArrayList(capability.getName(), getter);
			name = capability.getName() + " + " + getter.getName();
		}else{
			pipeline = Lists.<Serializable>newArrayList(new OutputSelector(capability, output));
			
			if (getter != null){
				pipeline.add(getter);
			}
			
			name = capability.getName();
		}
		
		return new DynamicSerializablePipeline(
				name, name,
				pipeline,
				CapabilityUtil.noArgs(pipeline.size()));
	}

	public ICapability getCapability() {
		return capability;
	}

	public IExtractCapability<?, ?> getGetter() {
		return getter;
	}
	
	public static boolean isGetter(Method method){
		return method.getParameterTypes().length == 0
				&& !FILTER.contains(method.getName())
				&& method.getReturnType() != Void.TYPE
				&& (method.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC;
	}
	
	public static List<DerivedCapability> derived(ICapability capability){
		List<DerivedCapability> result = Lists.newLinkedList();
		for (IOutput<?> output : capability.getOutputs()){
			result.add(new DerivedCapability(capability, output));
		}	
		return result;
	}
	
	public static List<DerivedCapability> derived(ICapability capability, IOutput<?> output){
		List<DerivedCapability> result = Lists.newLinkedList();
		result.addAll(derivedFields(capability, output));
		result.addAll(derivedProjections(capability, output));
		return result;
	}
	
	public static List<DerivedCapability> derivedFields(ICapability capability, IOutput<?> output){
		List<DerivedCapability> result = Lists.newLinkedList();
		
		Class<?> clazz = output.getType().getRawType();

		for (Method method : clazz.getMethods()){
			if (isGetter(method)){
				Getter<?, ?> getter = new Getter(
						method.getName(), 
						TypeToken.of(clazz), 
						TypeManager.boxType(TypeToken.of(method.getReturnType())));

				result.add(new DerivedCapability(capability, output, getter));
			}
		}
		
		return result;	
	}
	
	public static List<DerivedCapability> derivedProjections(ICapability capability, IOutput<?> output){
		List<DerivedCapability> result = Lists.newLinkedList();
		
		Class<?> clazz = output.getType().getRawType();

		// TODO refactor

		if (List.class.isAssignableFrom(clazz)){
			ParameterizedType outputType = (ParameterizedType) output.getType().getType();
			Type elementType = outputType.getActualTypeArguments()[0];

			if (elementType instanceof Class){
				Class<?> elementClass = (Class<?>) elementType;
				for (Method method : elementClass.getMethods()){
					if (isGetter(method)){
						ListGetter<?, ?> getter = new ListGetter(
								method.getName(), 
								TypeToken.of(elementClass), 
								TypeManager.boxType(TypeToken.of(method.getReturnType())));

						result.add(new DerivedCapability(capability, output, getter));
					}
				}
			}
		}else if (Set.class.isAssignableFrom(clazz)){
			ParameterizedType outputType = (ParameterizedType) output.getType().getType();
			Type elementType = outputType.getActualTypeArguments()[0];

			if (elementType instanceof Class){
				Class<?> elementClass = (Class<?>) outputType.getActualTypeArguments()[0];
				for (Method method : elementClass.getMethods()){
					if (isGetter(method)){
						SetGetter<?, ?> getter = new SetGetter(
								method.getName(), 
								TypeToken.of(elementClass), 
								TypeManager.boxType(TypeToken.of(method.getReturnType())));
						result.add(new DerivedCapability(capability, output, getter));
					}
				}
			}
		}


		return result;
	}

	public IOutput<?> getOutput() {
		return output;
	}
	
}
