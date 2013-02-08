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
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;

/**
 * Helper class for combination of Capability + Getter
 * @author Todd Schiller
 */
public class DerivedCapability{
	private final ICapability capability;
	private final IExtractCapability<?,? > getter;
	
	private static final Set<String> FILTER = Sets.newHashSet( 
			"hashCode" , "getClass" , "toString", "iterator", "listIterator", "toArray");
	
	public DerivedCapability(ICapability capability, IExtractCapability<?, ?> getter) {
		this.capability = capability;
		this.getter = getter;
	}
	
	public DynamicSerializablePipeline toPipeline(){
		List<Object> pipeline = Lists.<Object>newArrayList(
				capability.getUniqueId(),
				getter);
		
		return new DynamicSerializablePipeline(
				capability.getName() + " + " + getter.getName(), 
				capability.getName() + " + " + getter.getName(), 
				pipeline);
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
		result.addAll(derivedFields(capability));
		result.addAll(derivedProjections(capability));
		return result;
	}
	
	public static List<DerivedCapability> derivedFields(ICapability capability){
		List<DerivedCapability> result = Lists.newLinkedList();
		
		if (capability.getOutputs().size() == 1){
			IOutput<?> output = CapabilityUtil.singleOutput(capability);
			Class<?> clazz = output.getType().getRawType();
			
			for (Method method : clazz.getMethods()){
				if (isGetter(method)){
					Getter<?, ?> getter = new Getter(
							method.getName(), 
							TypeToken.of(clazz), 
							TypeManager.boxType(TypeToken.of(method.getReturnType())));
					
					result.add(new DerivedCapability(capability, getter));
				}
			}
		} else {
			throw new UnsupportedOperationException("Derived fields not supported for capabilities with multiple outputs");
		}
		
		return result;	
	}
	
	public static List<DerivedCapability> derivedProjections(ICapability capability){
		List<DerivedCapability> result = Lists.newLinkedList();
		
		if (CapabilityUtil.hasSingleOutput(capability)){
			IOutput<?> output = CapabilityUtil.singleOutput(capability);
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
							
							result.add(new DerivedCapability(capability, getter));
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
							result.add(new DerivedCapability(capability, getter));
						}
					}
				}
			}
		} else {
			throw new UnsupportedOperationException("Derived fields not supported for capabilities with multiple outputs");
		}
		
		return result;
	}
	
}
