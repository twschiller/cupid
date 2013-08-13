/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
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
public class CapabilityStatus extends Status {

	// TODO Let other plugins specify their plugin id in the status?
	
	/**
	 * The result of the capability.
	 */
	private final ICapabilityOutputs value;
	
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
	public CapabilityStatus(final int severity, final String message, final ICapabilityOutputs value) {
		super(severity, CupidActivator.PLUGIN_ID, message, null);
		this.value = value;
	}

	public CapabilityStatus(final Throwable exception) {
		super(Status.OK, CupidActivator.PLUGIN_ID, Status.ERROR, null, exception);
		this.value = null;
	}
	
	public CapabilityStatus(final ICapabilityOutputs value) {
		super(Status.OK, CupidActivator.PLUGIN_ID, null);
		this.value = value;
	}
	
	/**
	 * Returns the result of the capability, or <code>null</code> if the job
	 * was cancelled or threw an error.
	 * @return the result of the capability, or <code>null</code> if the job
	 * was cancelled or threw an error.
	 */
	public final ICapabilityOutputs value() {
		return this.value;
	}
	
	/**
	 * A convenience method for constructing a cancelled job status.
	 * @return a cancelled job status
	 */
	public static CapabilityStatus makeCancelled() {
		return new CapabilityStatus(Status.CANCEL, null, (Throwable) null);
	}
	
	/**
	 * A convenience method for constructing a successful job status.
	 * @param value the result of the computation
	 * @return a successful job status with result <code>value</code>
	 */
	public static CapabilityStatus makeOk(final ICapabilityOutputs value) {
		if (value == null) {
			throw new IllegalArgumentException("Capability results may not be null");
		}
		return new CapabilityStatus(value);
	}
	
	/**
	 * A convenience method for constructing an exceptional job status.
	 * @param exception the exception
	 * @return an exceptional job status with result <code>exception</code>
	 */
	public static CapabilityStatus makeError(final Throwable exception) {
		if (exception == null) {
			throw new IllegalArgumentException("Error capability results must have an associated exception");
		}
		
		return new CapabilityStatus(exception);
	}
}
