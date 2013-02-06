package edu.washington.cs.cupid.capability.linear;

import org.eclipse.core.runtime.Status;

import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;

public class LinearStatus<V> extends CapabilityStatus {
	
	private final V value;
	
	public LinearStatus(ILinearCapability<?, V> capability, V value) {
		super(CapabilityUtil.singletonOutput(capability.getOutput(), value));
		this.value = value;
	}
	
	public LinearStatus(Throwable error) {
		super(error);
		this.value = null;
	}

	/**
	 * Simplified constructor of a new status object; assumes that code is <code>OK</code>.
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or <code>CANCEL</code>
	 * @param message a human-readable message, localized to the current locale
	 * @param exception a low-level exception, or <code>null</code> if not applicable
	 */
	public LinearStatus(final int severity, final String message, final Throwable exception) {
		super(severity, message, exception);
		this.value = null;
	}
	
	/**
	 * A convenience method for constructing a cancelled job status.
	 * @return a cancelled job status
	 */
	public static <V> LinearStatus<V> makeCancelled() {
		return new LinearStatus<V>(Status.CANCEL, null, (Throwable) null);
	}
	
	/**
	 * A convenience method for constructing a successful job status.
	 * @param value the result of the computation
	 * @return a successful job status with result <code>value</code>
	 */
	public static <V> LinearStatus<V> makeOk(final ILinearCapability<?, V> capability, final V value) {
		if (value == null) {
			throw new IllegalArgumentException("Capability results may not be null");
		}
		return new LinearStatus<V>(capability, value);
	}
	
	/**
	 * A convenience method for constructing an exceptional job status.
	 * @param exception the exception
	 * @return an exceptional job status with result <code>exception</code>
	 */
	public static <V> LinearStatus<V> makeError(final Throwable exception) {
		if (exception == null) {
			throw new IllegalArgumentException("Error capability results must have an associated exception");
		}
		
		return new LinearStatus<V>(exception);
	}

	public V getOutputValue() {
		return value;
	}
}
