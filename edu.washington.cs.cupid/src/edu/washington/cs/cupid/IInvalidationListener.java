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
package edu.washington.cs.cupid;

import java.util.Set;

import org.eclipse.core.resources.IResourceChangeEvent;

/**
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface IInvalidationListener {
	
	/**
	 * Callback when a resource change causes invalidation.
	 * @param invalidated the objects evicted from the cache
	 * @param event the original resource change event
	 */
	void onResourceChange(Set<Object> invalidated, IResourceChangeEvent event);
}
