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

/**
 * Thrown to indicate that a capability is malformed (e.g., a job could not be created).
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class MalformedCapabilityException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final ICapability<?, ?> capability;
	
	/**
	 * Construct an exception indicating that <code>capability</code> is malformed.
	 * @param capability the capability
	 * @param msg the detail message
	 */
	public MalformedCapabilityException(final ICapability<?, ?> capability, final String msg) {
		super(msg);
		this.capability = capability;
	}
	
	/**
	 * Construct an exception indicating that <code>capability</code> is malformed.
	 * @param capability the capability
	 * @param cause the cause
	 */
	public MalformedCapabilityException(final ICapability<?, ?> capability, final Throwable cause) {
		super(cause);
		this.capability = capability;
	}

	/**
	 * Returns the capability that caused the exception.
	 * @return the capability that caused the exception.
	 */
	public final ICapability<?, ?> getCapability() {
		return capability;
	}
}
