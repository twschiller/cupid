package edu.washington.cs.cupid.capability;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability.Output;

public class OutputImpl<I> implements Output<I> {

	private String name;
	private TypeToken<I> type;
	
	public OutputImpl(String name, TypeToken<I> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeToken<I> getType() {
		return type;
	}

}
