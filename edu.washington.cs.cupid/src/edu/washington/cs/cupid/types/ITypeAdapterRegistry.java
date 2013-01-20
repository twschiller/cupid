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
package edu.washington.cs.cupid.types;

import com.google.common.reflect.TypeToken;

/**
 * Type adapter registry interface.
 * @see ITypeAdapter
 * @author Todd Schiller
 */
public interface ITypeAdapterRegistry {
	
	/**
	 * Add <code>adapter</code> to the registy.
	 * @param adapter the type adapter to add to the registry.
	 */
	void registerAdapter(final ITypeAdapter<?, ?> adapter);
	
	/**
	 * Returns the type adapters compatible with <code>inputType</code>.
	 * @param inputType the input object type query
	 * @see {@link TypeManager#isCompatible(edu.washington.cs.cupid.capability.ICapability, TypeToken)}
	 * @return the type adapters compatible with <code>inputType</code>.
	 */
	ITypeAdapter<?, ?>[] getTypeAdapters(final TypeToken<?> inputType);
	
	/**
	 * Returns the type adapters compatible with inputs of type <code>inputType</code>
	 * and producing outputs compatible with type <code>outputType</code>.
	 * @param inputType the input object type query
	 * @param outputType the input object type query
	 * @see {@link TypeManager#isCompatible(edu.washington.cs.cupid.capability.ICapability, TypeToken)}
	 * @return the type adapters compatible with both <code>inputType</code> and <code>outputType</code>.
	 */
	ITypeAdapter<?, ?> getTypeAdapter(final TypeToken<?> inputType, final TypeToken<?> outputType);
}
