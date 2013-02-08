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

import java.io.Serializable;

import com.google.common.reflect.TypeToken;

public class Parameter<T> implements ICapability.IParameter<T>, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final TypeToken<T> type;
	
	public Parameter(final String name, final TypeToken<T> type){
		this.name = name;
		this.type = type;
	}
	
	public Parameter(final String name, final Class<T> type){
		this(name, TypeToken.of(type));
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeToken<T> getType() {
		return type;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public T getDefault() {
		throw new IllegalArgumentException("Parameter does not have a default value");
	}

	@Override
	public boolean hasDefault() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Parameter)) {
			return false;
		}
		Parameter<?> other = (Parameter<?>) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
}
