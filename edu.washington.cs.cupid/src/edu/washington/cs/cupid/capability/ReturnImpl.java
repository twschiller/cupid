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

import edu.washington.cs.cupid.capability.ICapability.Output;

public class ReturnImpl implements ICapabilityOutput {

	private final Map<Output<?>, Object> outputs;
	private final Map<String, Object> named;
	
	public ReturnImpl(){
		outputs = Maps.newHashMap();
		named = Maps.newHashMap();
	}
	
	public <T> void add(final Output<T> output, final T value){
		outputs.put(output, value);
		named.put(output.getName(), value);
	}
	
	@Override
	public Map<Output<?>, Object> getOutputs() {
		return Collections.unmodifiableMap(outputs);
	}

	@Override
	public Object getOutput(final String name) {
		return named.get(name);
	}

	@Override
	public <T> T getOutput(final Output<T> output) {
		@SuppressWarnings("unchecked")
		T result = (T) outputs.get(output); // checked when building output
		return result;
	}

}
