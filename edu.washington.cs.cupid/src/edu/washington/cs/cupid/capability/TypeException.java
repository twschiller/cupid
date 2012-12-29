package edu.washington.cs.cupid.capability;

import com.google.common.reflect.TypeToken;

public class TypeException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	private final TypeToken<?> expected;
	private final TypeToken<?> actual;
	
	public TypeException(TypeToken<?> actual, TypeToken<?> expected) {
		this.expected = expected;
		this.actual = actual;
	}

	public TypeToken<?> getExpected() {
		return expected;
	}

	public TypeToken<?> getActual() {
		return actual;
	}
}
