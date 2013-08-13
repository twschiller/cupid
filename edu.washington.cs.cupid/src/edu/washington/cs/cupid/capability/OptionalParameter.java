package edu.washington.cs.cupid.capability;

import java.io.Serializable;

import com.google.common.reflect.TypeToken;

/**
 * An optional parameter with a default value.
 * @author Todd Schiller
 * @param <T> the parameter type
 */
public class OptionalParameter<T extends Serializable> extends AbstractParameter<T> {
	private static final long serialVersionUID = 1L;
	
	private final T def;
	
	/**
	 * Create an optional parameter
	 * @param name the name of the parameter
	 * @param type the type of the paramter
	 * @param def the default value
	 */
	public OptionalParameter(String name, TypeToken<T> type, T def){
		super(name, type);
		this.def = def;
	}

	/**
	 * Create an optional parameter
	 * @param name the name of the parameter
	 * @param type the type of the parameter
	 * @param def the default value
	 */
	public OptionalParameter(String name, Class<T> type, T def){
		this(name, TypeToken.of(type), def);
	}
	
	@Override
	public T getDefault() {
		return def;
	}

	@Override
	public boolean hasDefault() {
		return true;
	}
}
