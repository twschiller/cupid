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

import java.util.Map;

import com.google.common.collect.Maps;

import edu.washington.cs.cupid.capability.ICapability.Output;

public class ReturnImpl implements ICapabilityOutput{

	private Map<Output<?>, Object> outputs;
	private Map<String, Object> named;
	
	public ReturnImpl(){
		outputs = Maps.newHashMap();
		named = Maps.newHashMap();
	}
	
	public <T> void add(Output<T> output, T value){
		outputs.put(output, value);
		named.put(output.getName(), value);
	}
	
	@Override
	public Map<Output<?>, Object> getOutputs() {
		return outputs;
	}

	@Override
	public Object getOutput(String name) {
		return named.get(name);
	}

}
