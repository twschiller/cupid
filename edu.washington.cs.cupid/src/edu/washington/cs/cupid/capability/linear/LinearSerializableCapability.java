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
package edu.washington.cs.cupid.capability.linear;

import com.google.common.reflect.TypeToken;

public abstract class LinearSerializableCapability<I, V> extends GenericLinearSerializableCapability<I, V> implements ILinearCapability<I, V> {
	
	private static final long serialVersionUID = 1L;
	
	private TypeToken<I> inputType;
	private TypeToken<V> outputType;
	
	public LinearSerializableCapability(String name, String uniqueId,
			String description, 
			TypeToken<I> inputType, TypeToken<V> outputType,
			Flag... flags) {
		super(name, uniqueId, description, flags);
		
		this.inputType = inputType;
		this.outputType = outputType;
	}
	
	public LinearSerializableCapability(String name, String uniqueId,
			String description, 
			Class<I> inputType, Class<V> outputType,
			Flag... flags) {
		
		this(name, uniqueId, description, TypeToken.of(inputType), TypeToken.of(outputType), flags);
	}

	@Override
	public final TypeToken<I> getInputType() {
		return inputType;
	}

	@Override
	public final TypeToken<V> getOutputType() {
		return outputType;
	}

}
