package edu.washington.cs.cupid.capability;

import org.eclipse.core.runtime.Status;

import edu.washington.cs.cupid.internal.CupidActivator;

/**
 * Status of a {@link CapabilityJob}. If {@link #getCode()} is <code>Status.OK</code>, then call
 * {@link #value()} to get the result of the computation. If the status is <code>Status.ERROR</code>
 * call {@link #getException()} to get the exception.
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <V> result type
 */
public class CapabilityStatus<V> extends Status {

	// TODO Let other plugins specify their plugin id in the status?
	
	/**
	 * The result of the capability.
	 */
	private final V value;
	
	/**
	 * Simplified constructor of a new status object; assumes that code is <code>OK</code>.
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or <code>CANCEL</code>
	 * @param message a human-readable message, localized to the current locale
	 * @param exception a low-level exception, or <code>null</code> if not applicable
	 */
	public CapabilityStatus(final int severity, final String message, final Throwable exception) {
		super(severity, CupidActivator.PLUGIN_ID, message, exception);
		this.value = null;
	}

	/**
	 * Simplified constructor of a new status object; assumes that code is <code>OK</code>.
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or <code>CANCEL</code>
	 * @param message a human-readable message, localized to the current locale
	 * @param value the capability output
	 */
	public CapabilityStatus(final int severity, final String message, final V value) {
		super(severity, CupidActivator.PLUGIN_ID, message, null);
		this.value = value;
	}

	private CapabilityStatus(final Throwable exception) {
		super(Status.OK, CupidActivator.PLUGIN_ID, Status.ERROR, null, exception);
		this.value = null;
	}
	
	private CapabilityStatus(final V value) {
		super(Status.OK, CupidActivator.PLUGIN_ID, null);
		this.value = value;
	}
	
	/**
	 * Returns the result of the capability, or <code>null</code> if the job
	 * was cancelled or threw an error.
	 * @return the result of the capability, or <code>null</code> if the job
	 * was cancelled or threw an error.
	 */
	public final V value() {
		return this.value;
	}
	
	/**
	 * A convenience method for constructing a cancelled job status.
	 * @param <V> the capability result type
	 * @return a cancelled job status
	 */
	public static <V> CapabilityStatus<V> makeCancelled() {
		return new CapabilityStatus<V>(Status.CANCEL, null, (Throwable) null);
	}
	
	/**
	 * A convenience method for constructing a successful job status.
	 * @param value the result of the computation
	 * @param <V> the result type
	 * @return a successful job status with result <code>value</code>
	 */
	public static <V> CapabilityStatus<V> makeOk(final V value) {
		if (value == null) {
			throw new IllegalArgumentException("Capability results may not be null");
		}
		return new CapabilityStatus<V>(value);
	}
	
	/**
	 * A convenience method for constructing an exceptional job status.
	 * @param exception the exception
	 * @param <V> the capability result type
	 * @return an exceptional job status with result <code>exception</code>
	 */
	public static <V> CapabilityStatus<V> makeError(final Throwable exception) {
		if (exception == null) {
			throw new IllegalArgumentException("Error capability results must have an associated exception");
		}
		
		return new CapabilityStatus<V>(exception);
	}
}
