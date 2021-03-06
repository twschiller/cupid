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

public abstract class LinearCapability<I, V> extends GenericLinearCapability<I, V> implements ILinearCapability<I, V> {
	
	private TypeToken<I> inputType;
	private TypeToken<V> outputType;
	
	public LinearCapability(String name, String description, 
			TypeToken<I> inputType, TypeToken<V> outputType,
			Flag... flags) {
		super(name, description, flags);
		
		this.inputType = inputType;
		this.outputType = outputType;
	}
	
	public LinearCapability(String name,
			String description, 
			Class<I> inputType, Class<V> outputType,
			Flag... flags) {
		
		this(name, description, TypeToken.of(inputType), TypeToken.of(outputType), flags);
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
