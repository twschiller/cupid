package edu.washington.cs.cupid.capability;

import java.io.Serializable;

import com.google.common.reflect.TypeToken;

public class OptionalParameter<T extends Serializable> extends AbstractParameter<T> {
	private static final long serialVersionUID = 1L;
	
	private final T def;
	
	public OptionalParameter(String name, TypeToken<T> type, T def){
		super(name, type);
		this.def = def;
	}

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
