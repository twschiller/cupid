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
package edu.washington.cs.cupid.capability.options;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;

/**
 * An {@link ICapability} option; may be required or optional. Required options have default values, optional
 * options may or may not have a default value. 
 * @author Todd Schiller
 * @param <T> the option type
 * @see {@link IConfigurableCapability}, {@link ICapability}
 */
public final class Option<T> {

	private final String name;
	private final TypeToken<T> type;
	private final boolean optional;
	private final T def;
	
	/**
	 * Construct an option definition.
	 * @param name the name of the option
	 * @param type the type of the option
	 * @param def the default value of the option
	 * @param optional <code>true</code> if the option is optional
	 */
	public Option(final String name, final TypeToken<T> type, final T def, final boolean optional){
		this.name = name;
		this.type = type;
		this.optional = optional;
		this.def = def;
	}
	
	/**
	 * Construct an option definition.
	 * @param name the name of the option
	 * @param type the type of the option
	 * @param def the default value of the option
	 * @param optional <code>true</code> if the option is optional
	 */
	public Option(final String name, final Class<T> type, final T def, final boolean optional){
		this(name, TypeToken.of(type), def, optional);
	}
	
	/**
	 * Construct a required option definition.
	 * @param name the name of the option
	 * @param type the type of the option
	 * @param def the default value of the option
	 * @param optional
	 */
	public Option(final String name, final TypeToken<T> type, final T def){
		this(name, type, def, false);
	}

	/**
	 * Construct a required option definition.
	 * @param name the name of the option
	 * @param type the type of the option
	 * @param def the default value of the option
	 * @param optional
	 */
	public Option(final String name, final Class<T> type, final T def){
		this(name, TypeToken.of(type), def);
	}
	
	/**
	 * Returns the name of the option.
	 * @return the name of the option
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of the option.
	 * @return the type of the option
	 */
	public TypeToken<T> getType() {
		return type;
	}

	/**
	 * Returns <code>true</code> if the option is optional.
	 * @return <code>true</code> if the option is optional.
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * Returns the option's default value, or <code>null</code> if the option
	 * is optional and has no default value.
	 * @return the option's default value, or <code>null</code>
	 */
	public T getDefault() {
		return def;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((def == null) ? 0 : def.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Option<?> other = (Option<?>) obj;
		if (def == null) {
			if (other.def != null)
				return false;
		} else if (!def.equals(other.def))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (optional != other.optional)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	

	
}
