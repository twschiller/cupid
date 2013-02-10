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

import com.google.common.reflect.TypeToken;

public class Parameter<T> extends AbstractParameter<T> {
	private static final long serialVersionUID = 1L;
	
	public Parameter(final String name, final TypeToken<T> type){
		super(name, type);
	}
	
	public Parameter(final String name, final Class<T> type){
		this(name, TypeToken.of(type));
	}
	
	@Override
	public T getDefault() {
		throw new IllegalArgumentException("Parameter does not have a default value");
	}

	@Override
	public boolean hasDefault() {
		return false;
	}
}
