package edu.washington.cs.cupid.capability;

import com.google.common.reflect.TypeToken;

/**
 * An exception indicating a type mismatch found at runtime.
 * @author Todd Schiller
 */
public class TypeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final TypeToken<?> expected;
	private final TypeToken<?> actual;
	
	/**
	 * An exception indicating a type mismatch found at runtime.
	 * @param actual the actual type
	 * @param expected the expected type
	 */
	public TypeException(final TypeToken<?> actual, final TypeToken<?> expected) {
		this.expected = expected;
		this.actual = actual;
	}

	/**
	 * Returns the expected type.
	 * @return the expected type
	 */
	public final TypeToken<?> getExpected() {
		return expected;
	}

	/**
	 * Returns the actual type.
	 * @return the actual type
	 */
	public final TypeToken<?> getActual() {
		return actual;
	}
}
