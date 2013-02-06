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
import edu.washington.cs.cupid.capability.InputImpl;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class ValueMapping<I,V> extends AbstractMapping<I,I,V> implements IDynamicCapability {

	private static final long serialVersionUID = 1L;
	
	private final static String BASE_ID = "edu.washington.cs.cupid.wizards.internal.mapping.value";
		
	private ISerializableCapability valueGenerator;
	private String keyLink;
	private String valueLink;
	
	public ValueMapping(
			String name,
			String description,
			TypeToken<I> inputType, String keyLink,
			ISerializableCapability valueGenerator, TypeToken<V> valueType, String valueLink){
	
		super(name, description, 
			 BASE_ID + ".[" + inputType.getRawType().getName() + "].[" + valueGenerator.getUniqueId() + "]",
			 inputType, inputType, valueType,
			 valueGenerator.getFlags());
		
		this.valueGenerator = valueGenerator;
		this.keyLink = keyLink;
		this.valueLink = valueLink;
	}
	
	@Override
	public Set<String> getDynamicDependencies() {
		if (valueGenerator instanceof IDynamicCapability){
			return ((IDynamicCapability) valueGenerator).getDynamicDependencies();
		} else {
			return Sets.newHashSet();
		}
	}

	private Object link(Object value, String valueLink) throws Exception {
		if (valueLink == null){
			return value;
		} else {
			Method method = value.getClass().getMethod(valueLink);
			return method.invoke(value);
		}
	}

	@Override
	public LinearJob<I, Map<I, Set<V>>> getJob(I input) {
		return new LinearJob<I, Map<I,Set<V>>>(this, input){
			@Override
			protected LinearStatus<Map<I, Set<V>>> run(final IProgressMonitor monitor) {
				try{
					monitor.beginTask(getName(), 100);
					
					Object key = link(getInput(), keyLink);

					CapabilityJob<?> subtask = valueGenerator.getJob(new InputImpl());
					monitor.subTask("Generating Values");
					
					subtask.schedule();
					subtask.join();
					
					CapabilityStatus status = (CapabilityStatus) subtask.getResult();
					if (!status.isOK()){
						throw status.getException();
					}
					
					Collection<V> values = (Collection<V>) CapabilityUtil.singleOutputValue(valueGenerator, status);
					
					monitor.worked(50);
					
					monitor.subTask("Linking Key and Values");
					Set<V> collection = Sets.newHashSet();
					for (V v : values){
						if (key.equals(link(v, valueLink))){
							collection.add(v);
						}
					}

					Map<I, Set<V>> result = Maps.newHashMap();
					result.put((I) getInput(), collection);

					return LinearStatus.makeOk(getCapability(), result);

				} catch(Throwable ex) {
					return LinearStatus.<Map<I, Set<V>>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
