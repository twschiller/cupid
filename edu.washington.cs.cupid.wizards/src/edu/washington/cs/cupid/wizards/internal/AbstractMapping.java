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

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.GenericLinearSerializableCapability;

public abstract class AbstractMapping<I, K, V> extends GenericLinearSerializableCapability<I, Map<K,Set<V>>>  {

	private static final long serialVersionUID = 2L;

	protected final TypeToken<K> keyType;
	protected final TypeToken<I> inputType;
	protected final TypeToken<V> valueType;
	
	public AbstractMapping(String name, String description,  
			TypeToken<I> inputType, TypeToken<K> keyType, TypeToken<V> valueType,
			EnumSet<Flag> flags){
		
		super(name, description, flags.toArray(new Flag[]{}));
			  
		this.inputType = inputType;
		this.keyType = keyType;
		this.valueType = valueType;
	}
		
	@Override
	public final TypeToken<I> getInputType() {
		return inputType;
	}

	@Override
	public final TypeToken<Map<K,Set<V>>> getOutputType() {
		 return new TypeToken<Map<K,Set<V>>>(getClass()){}
		 	.where(new TypeParameter<K>(){}, keyType)
		 	.where(new TypeParameter<V>(){}, valueType);
	}
}
