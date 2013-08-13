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

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * <p>Capability change notifier that automatically rebroadcasts the changes from
 * any {@link ICapabilityPublisher}s it is listening to.</p>
 * 
 * <p>Does <i>not</i> check for notification loops.</p>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class ChangeNotifier implements ICapabilityChangeListener, ICapabilityChangeNotifier { 

	private final Set<ICapabilityChangeListener> listeners = Sets.newIdentityHashSet();
		
	/**
	 * Construct a notifier that rebroadcasts changes from the {@link ICapabilityPublisher}s 
	 * it is listening to.
	 */
	public ChangeNotifier() {
		super();
	}
	
	@Override
	public final synchronized void addChangeListener(final ICapabilityChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public final synchronized void removeChangeListener(final ICapabilityChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public final synchronized void onCapabilityAdded(ICapability capability) {
		for (final ICapabilityChangeListener listener : listeners) {
			listener.onCapabilityAdded(capability);
		}
	}

	@Override
	public final synchronized void onCapabilityRemoved(ICapability capability) {
		for (final ICapabilityChangeListener listener : listeners) {
			listener.onCapabilityRemoved(capability);
		}
	}
}
