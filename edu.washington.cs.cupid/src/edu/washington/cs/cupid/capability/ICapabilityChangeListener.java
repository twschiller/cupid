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
 * <p>Listen for changes to the set of capabilities provided by an {@link ICapabilityPublisher}.</p>
 * 
 * <p>Since notification is synchronous with the activity itself, the listener should provide a fast and robust implementation. 
 * If the handling of notifications would involve blocking operations, or operations which might throw uncaught exceptions, 
 * the notifications should be queued, and the actual processing deferred (or perhaps delegated to a separate thread).</p>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @see {@link ICapabilityPublisher} the capability publishing interface
 * @see {@link ChangeNotifier} a thread-safe notification implementation
 */
public interface ICapabilityChangeListener {
	
	/**
	 * Triggered when the <tt>publisher</tt> provides a new capability.
	 * @param capability
	 */
	void onCapabilityAdded(ICapability capability);
	
	/**
	 * Triggered when the <tt>publisher</tt> no longer provides a capability.
	 * @param capability
	 */
	void onCapabilityRemoved(ICapability capability);
}
