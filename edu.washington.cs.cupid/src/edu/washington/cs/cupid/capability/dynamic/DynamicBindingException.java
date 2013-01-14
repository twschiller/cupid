package edu.washington.cs.cupid.capability.dynamic;

/**
 * Thrown to indicate that a dynamic capability binding failed (e.g., the capability
 * in the environment had the wrong type).
 * @author Todd Schiller
 */
public class DynamicBindingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct an exception indicating that a dynamic capability binding failed.
	 * @param cause the cause
	 */
	public DynamicBindingException(final Throwable cause) {
		super(cause);
	}

}
