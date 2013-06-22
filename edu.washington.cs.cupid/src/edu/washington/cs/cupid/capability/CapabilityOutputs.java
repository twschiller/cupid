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

import edu.washington.cs.cupid.capability.ICapability.IOutput;

/**
 * The outputs produced by a capability.
 * @author Todd Schiller
 */
public class CapabilityOutputs implements ICapabilityOutputs {

	private final Map<IOutput<?>, Object> outputs;
	private final Map<String, Object> named;
	
	public CapabilityOutputs(){
		outputs = Maps.newHashMap();
		named = Maps.newHashMap();
	}
	
	/**
	 * Add the output {@code value} for {@code output}.  
	 * @param output the output reference 
	 * @param value the output value
	 */
	public <T> void add(final IOutput<T> output, final T value){
		outputs.put(output, value);
		named.put(output.getName(), value);
	}
	
	@Override
	public Map<IOutput<?>, Object> getOutputs() {
		return Collections.unmodifiableMap(outputs);
	}

	@Override
	public Object getOutput(final String name) {
		return named.get(name);
	}

	@Override
	public <T> T getOutput(final IOutput<T> output) {
		@SuppressWarnings("unchecked") // checked when building output
		T result = (T) outputs.get(output); 
		return result;
	}

}
