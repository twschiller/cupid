package edu.washington.cs.cupid.capability.linear;

import org.eclipse.core.runtime.Status;

import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;

public class LinearStatus extends CapabilityStatus {
	
	private final Object value;
	
	public LinearStatus(ICapability capability, Object value) {
		super(CapabilityUtil.singletonOutput(capability, value));
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
	public static LinearStatus makeCancelled() {
		return new LinearStatus(Status.CANCEL, null, (Throwable) null);
	}
	
	/**
	 * A convenience method for constructing a successful job status.
	 * @param value the result of the computation
	 * @return a successful job status with result <code>value</code>
	 */
	public static LinearStatus makeOk(final ICapability capability, final Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Capability results may not be null");
		}
		return new LinearStatus(capability, value);
	}
	
	/**
	 * A convenience method for constructing an exceptional job status.
	 * @param exception the exception
	 * @return an exceptional job status with result <code>exception</code>
	 */
	public static LinearStatus makeError(final Throwable exception) {
		if (exception == null) {
			throw new IllegalArgumentException("Error capability results must have an associated exception");
		}
		
		return new LinearStatus(exception);
	}

	public Object getOutputValue() {
		return value;
	}
}
