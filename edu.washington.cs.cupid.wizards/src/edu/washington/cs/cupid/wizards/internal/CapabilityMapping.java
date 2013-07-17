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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.IDynamicCapability;
import edu.washington.cs.cupid.capability.ISerializableCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class CapabilityMapping<I, K, V> extends AbstractMapping<I, K, V> implements IDynamicCapability {

	private static final long serialVersionUID = 1L;
	private final static String BASE_ID = "edu.washington.cs.cupid.wizards.internal.mapping.capability";

	private ISerializableCapability inputGenerator;
	private ISerializableCapability valueGenerator;
	private String keyLink;
	private String valueLink;
	
	public CapabilityMapping(
			String name,
			String description,
			TypeToken<I> inputType,
			ISerializableCapability inputGenerator, TypeToken<K> keyType , String keyLink,
			ISerializableCapability valueGenerator, TypeToken<V> valueType, String valueLink){
		super(name, description, 
			  inputType, keyType, valueType,
			  CapabilityUtil.mergeFlags(inputGenerator, valueGenerator));
		
		this.inputGenerator = inputGenerator;
		this.keyLink = keyLink;
		this.valueGenerator = valueGenerator;
		this.valueLink = valueLink;
	}
	

	@Override
	public Set<String> getDynamicDependencies() {
		Set<String> result = Sets.newHashSet();
		
		if (inputGenerator instanceof IDynamicCapability){
			result.addAll(((IDynamicCapability) inputGenerator).getDynamicDependencies());
		}
		
		if (valueGenerator instanceof IDynamicCapability){
			result.addAll(((IDynamicCapability) valueGenerator).getDynamicDependencies());
		}

		return result;
	}

	private Object link(Object value, String valueLink) throws Exception {
		if (valueLink == null) {
			return value;
		} else {
			Method method = value.getClass().getMethod(valueLink);
			if (!method.isAccessible()){
				method.setAccessible(true);
			}
			return method.invoke(value);
		}
	}
	
	
	@Override
	public LinearJob<I, Map<K, Set<V>>> getJob(I input) {		
		return new LinearJob<I, Map<K, Set<V>>>(this, input){
			@Override
			protected LinearStatus<Map<K, Set<V>>> run(final IProgressMonitor monitor) {
				try{
					monitor.beginTask(getName(), 30);
					
					CapabilityJob<?> keySubtask = inputGenerator.getJob(CapabilityUtil.packUnaryInput(inputGenerator, getInput()));
					CapabilityJob<?> valueSubtask = valueGenerator.getJob(CapabilityUtil.packUnaryInput(valueGenerator, getInput()));

					monitor.subTask("Generating Keys");
					keySubtask.schedule();
					keySubtask.join();
					
					CapabilityStatus keyStatus = (CapabilityStatus) keySubtask.getResult();
					if (!keyStatus.isOK() || keyStatus.getException() != null){
						throw keyStatus.getException();
					}
					
					Collection<K> keys = (Collection<K>) CapabilityUtil.singleOutputValue(inputGenerator, keyStatus);
					monitor.worked(10);
					
					monitor.subTask("Generating Values");
					valueSubtask.schedule();
					valueSubtask.join();
					
					CapabilityStatus status = (CapabilityStatus) valueSubtask.getResult();
					if (!status.isOK() || status.getException() != null){
						throw status.getException();
					}
					
					Collection<V> values = (Collection<V>) CapabilityUtil.singleOutputValue(valueGenerator, status);
					monitor.worked(10);
					
					monitor.subTask("Linking Keys and Values");

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

					return LinearStatus.makeOk(getCapability(), result);

				}catch(Throwable t){
					return LinearStatus.<Map<K, Set<V>>>makeError(t);
				}finally{
					monitor.done();
				}
			}
		};
	}
}
