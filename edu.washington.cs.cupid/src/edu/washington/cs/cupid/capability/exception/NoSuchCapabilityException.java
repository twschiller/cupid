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
package edu.washington.cs.cupid.capability.exception;

/**
 * Thrown to indicate that a capability is not available.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class NoSuchCapabilityException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final String capabilityId;

	/**
	 * Construct an exception indicating that <code>capabilityId</code> is not available.
	 * @param capabilityId the id of the capability
	 */
	public NoSuchCapabilityException(final String capabilityId) {
		super();
		this.capabilityId = capabilityId;
	}

	/**
	 * Returns the id of the missing capability.
	 * @return the id of the missing capability
	 */
	public final String getCapabilityId() {
		return capabilityId;
	}
}
