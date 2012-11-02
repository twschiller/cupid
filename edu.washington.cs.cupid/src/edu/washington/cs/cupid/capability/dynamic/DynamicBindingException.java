package edu.washington.cs.cupid.capability.dynamic;

/**
 * Thrown to indicate that a dynamic capability binding failed (e.g., the capability
 * in the environment had the wrong type).
 * @author Todd Schiller
 */
public class DynamicBindingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final Throwable inner;

	public DynamicBindingException(Throwable inner) {
		super();
		this.inner = inner;
	}

	public Throwable getInner() {
		return inner;
	}	
}
