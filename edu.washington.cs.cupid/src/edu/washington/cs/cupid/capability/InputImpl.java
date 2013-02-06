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

import edu.washington.cs.cupid.capability.ICapability.Parameter;

public class InputImpl implements ICapabilityInput {

	private Map<Parameter<?>, Object> arguments;
	private Map<String, Object> named;
	
	public InputImpl(){
		arguments = Maps.newHashMap();
		named = Maps.newHashMap();
	}
	
	public <T> void add(Parameter<T> parameter, T argument){
		arguments.put(parameter, argument);
		named.put(parameter.getName(), argument);
	}
	
	@Override
	public <T> T getArgument(Parameter<T> parameter){
		@SuppressWarnings("unchecked") // checked in the add(...) method
		T result = (T) arguments.get(parameter);
		return result ;
	}
	
	@Override
	public Map<Parameter<?>, Object> getArguments() {
		return Collections.unmodifiableMap(arguments);
	}

	@Override
	public Object getArgument(String name) {
		return named.get(name);
	}

}
