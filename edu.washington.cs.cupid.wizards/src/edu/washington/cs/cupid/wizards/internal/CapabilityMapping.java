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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ISerializableCapability;

public class CapabilityMapping<I,K,V> extends AbstractMapping<I,K,V> {

	private static final long serialVersionUID = 1L;
	private final static String BASE_ID = "edu.washington.cs.cupid.wizards.internal.mapping.capability";

	private ISerializableCapability<I, Collection<K>> inputGenerator;
	private ISerializableCapability<I, Collection<V>> valueGenerator;
	private String keyLink;
	private String valueLink;
	
	public CapabilityMapping(
			String name,
			String description,
			TypeToken<I> inputType,
			ISerializableCapability<I, Collection<K>> inputGenerator, TypeToken<K> keyType , String keyLink,
			ISerializableCapability<I, Collection<V>> valueGenerator, TypeToken<V> valueType, String valueLink){
		super(name, description, inputType, keyType, valueType);
		
		this.inputGenerator = inputGenerator;
		this.keyLink = keyLink;
		this.valueGenerator = valueGenerator;
		this.valueLink = valueLink;
	}
	
	@Override
	public String getUniqueId() {
		return BASE_ID + ".[" + inputType.toString() + "].[" + inputGenerator.getUniqueId() + "].[" + valueGenerator.getUniqueId() + "]"; 
	}


	@Override
	public Set<String> getDynamicDependencies() {
		return Sets.union(inputGenerator.getDynamicDependencies(), valueGenerator.getDynamicDependencies());
	}

	private Object link(Object value, String valueLink) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		return valueLink == null 
				? value
				: value.getClass().getMethod(valueLink).invoke(value);
	}
	

	
	@Override
	public CapabilityJob<I, Map<K, Set<V>>> getJob(I input) {		
		return new CapabilityJob<I, Map<K, Set<V>>>(this, input){
			@Override
			protected CapabilityStatus<Map<K, Set<V>>> run(IProgressMonitor monitor) {
				try{
					CapabilityJob<I,Collection<K>> keySubtask = inputGenerator.getJob(input);
					CapabilityJob<I,Collection<V>> valueSubtask = valueGenerator.getJob(input);
					
					try{
						monitor.subTask("Generating Keys");
						Collection<K> keys = runSubtask(keySubtask, monitor);
						
						monitor.subTask("Generating Values");
						Collection<V> values = runSubtask(valueSubtask, monitor);
						
						monitor.subTask("Building Map");
						
						Map<K, Set<V>> result = Maps.newHashMap();
						
						for (K key : keys){
							Set<V> collection = Sets.newHashSet();
							
							for (V v : values){
								if (key != null && link(key, keyLink).equals(link(v, valueLink))){
									collection.add(v);
								}
							}
							result.put(key, collection);
						}
						
						return CapabilityStatus.makeOk(result);
						
					}catch(Throwable t){
						return CapabilityStatus.makeError(t);
					}finally{
						monitor.done();
					}
					
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}

	@Override
	public boolean isPure() {
		return inputGenerator.isPure() && valueGenerator.isPure();
	}

	@Override
	public boolean isLocal() {
		return inputGenerator.isLocal() && valueGenerator.isLocal();
	}

	@Override
	public boolean isTransient() {
		return inputGenerator.isTransient() || valueGenerator.isTransient();
	}

}
