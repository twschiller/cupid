package edu.washington.cs.cupid.capability;

import com.google.common.reflect.TypeToken;

/**
 * Base implementation of an immutable named parameter; to parameters are 
 * considered equivalent if their name and type are equivalent.
 * @author Todd Schiller
 * @param <T> the parameter type
 */
public abstract class AbstractParameter<T> implements ICapability.IParameter<T> {

	private static final long serialVersionUID = 1L;

	private final String name;
	private final TypeToken<T> type;
	
	/**
	 * Create an abstract parameter
	 * @param name the name of the parameter
	 * @param type the type of the parameter
	 */
	public AbstractParameter(final String name, final TypeToken<T> type){
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final TypeToken<T> getType() {
		return type;
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
		if (!(obj instanceof AbstractParameter)) {
			return false;
		}
		AbstractParameter<?> other = (AbstractParameter<?>) obj;
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
