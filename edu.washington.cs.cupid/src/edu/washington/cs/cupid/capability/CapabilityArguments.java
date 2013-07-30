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

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.washington.cs.cupid.capability.ICapability.IParameter;

public class CapabilityArguments implements ICapabilityArguments {

	public static final CapabilityArguments NONE = new CapabilityArguments(); 
	
	private Map<IParameter<?>, Object> arguments;
	
	public CapabilityArguments(){
		arguments = Maps.newIdentityHashMap();
	}
	
	public CapabilityArguments(ICapabilityArguments other){
		arguments = Maps.newIdentityHashMap();
		arguments.putAll(other.getArguments());
	}
	
	public <T> void add(IParameter<T> parameter, T argument){
		arguments.put(parameter, argument);
	}
	
	public void add(IParameter<?> parameter, ICapability capability){
		if (!CapabilityUtil.isSerializable(capability)){
			throw new IllegalArgumentException("Capability argument must be serializable; capability: " + capability.getName());
		}
		
		arguments.put(parameter, capability);
	}
	
	@Override
	public <T> T getValueArgument(IParameter<T> parameter){
		
		if (arguments.containsKey(parameter)) {
			Object result = arguments.get(parameter);
			if (result instanceof ICapability){
				throw new IllegalArgumentException("Parameter has capability argument");
			} else {
				return (T) result;
			}
			
		} else if (parameter.hasDefault()) {
			return parameter.getDefault();
		} else {
			throw new IllegalArgumentException("No argument supplied for parameter: " + parameter);
		}	
	}
	
	public boolean hasValueArgument(IParameter<?> parameter){
		if (arguments.containsKey(parameter)) {
			return !(arguments.get(parameter) instanceof ICapability);
		}else if (parameter.hasDefault()) {
			return true;
		} else {
			throw new IllegalArgumentException("No argument supplied for parameter: " + parameter);
		}	
	}
	
	@Override
	public Map<IParameter<?>, Object> getArguments() {
		return Collections.unmodifiableMap(arguments);
	}

	@Override
	public <T> ICapability getCapabilityArgument(IParameter<T> parameter) {
		if (arguments.containsKey(parameter)) {
			Object result = arguments.get(parameter);
			if (result instanceof ICapability){
				return (ICapability) result;
			} else {
				throw new IllegalArgumentException("Parameter has constant argument");
			}
		} else {
			throw new IllegalArgumentException("No argument supplied for parameter: " + parameter);
		}	
	}

}
