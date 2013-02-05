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

}
