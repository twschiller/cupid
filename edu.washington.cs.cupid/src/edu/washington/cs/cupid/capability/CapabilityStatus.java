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
	 * The result of the capability
	 */
	private final V value;
	
	public CapabilityStatus(int severity, String message, Throwable exception) {
		super(severity, CupidActivator.PLUGIN_ID, message, exception);
		this.value = null;
	}

	public CapabilityStatus(int severity, String message, V value) {
		super(severity, CupidActivator.PLUGIN_ID, message, null);
		this.value = value;
	}

	private CapabilityStatus(Throwable exception){
		super(Status.OK, CupidActivator.PLUGIN_ID, Status.ERROR, null, exception);
		this.value = null;
	}
	
	private CapabilityStatus(V value){
		super(Status.OK, CupidActivator.PLUGIN_ID, null);
		this.value = value;
	}
	
	/**
	 * @return the result of the capability, or <code>null</code> if the job
	 * was cancelled or threw an error.
	 */
	public V value(){
		return this.value;
	}
	
	/**
	 * @return a cancelled job status
	 */
	public static <V> CapabilityStatus<V> makeCancelled(){
		return new CapabilityStatus<V>(Status.CANCEL, null, (Throwable) null);
	}
	
	/**
	 * Make a successful job status
	 * @param value the result of the computation
	 * @return a successful job status with result <code>value</code>
	 */
	public static <V> CapabilityStatus<V> makeOk(V value){
		if (value == null){
			throw new IllegalArgumentException("Capability results may not be null");
		}
		return new CapabilityStatus<V>(value);
	}
	
	/**
	 * Make an exception job status
	 * @param exception the exception
	 * @return an exceptional job status with result <code>exception</code>
	 */
	public static <V> CapabilityStatus<V> makeError(Throwable exception){
		if (exception == null){
			throw new IllegalArgumentException("Error capability results must have an associated exception");
		}
		
		return new CapabilityStatus<V>(exception);
	}
}
