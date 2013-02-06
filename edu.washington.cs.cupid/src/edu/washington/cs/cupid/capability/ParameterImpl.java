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

public class ParameterImpl<T> implements ICapability.Parameter<T>{

	private final String name;
	private final TypeToken<T> type;
	private final T def;
	private final boolean hasDefault;

	private ParameterImpl(String name, TypeToken<T> type, T def, boolean hasDefault){
		this.name = name;
		this.type = type;
		this.def = def;
		this.hasDefault = hasDefault;
	}
	
	public ParameterImpl(String name, TypeToken<T> type, T def){
		this(name, type, def, true);
	}
	
	public ParameterImpl(String name, TypeToken<T> type){
		this(name, type, null, false);
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
	public T getDefault() {
		return def;
	}

	@Override
	public boolean hasDefault() {
		return hasDefault;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((def == null) ? 0 : def.hashCode());
		result = prime * result + (hasDefault ? 1231 : 1237);
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
		if (!(obj instanceof ParameterImpl)) {
			return false;
		}
		ParameterImpl<?> other = (ParameterImpl<?>) obj;
		if (def == null) {
			if (other.def != null) {
				return false;
			}
		} else if (!def.equals(other.def)) {
			return false;
		}
		if (hasDefault != other.hasDefault) {
			return false;
		}
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
