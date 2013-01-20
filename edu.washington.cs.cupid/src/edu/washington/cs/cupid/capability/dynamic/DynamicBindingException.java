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
