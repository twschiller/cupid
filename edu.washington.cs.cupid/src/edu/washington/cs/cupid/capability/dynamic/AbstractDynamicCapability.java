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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.IDynamicCapability;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;

/**
 * A capability with possibly dynamic bindings.
 * @author Todd Schiller
 * @param <I> input type
 * @param <V> output type
 */
public abstract class AbstractDynamicCapability implements IDynamicCapability {

	private final String name;
	private final String description;
	private final Set<Object> capabilities;
	
	/**
	 * Construct a capability with possibly dynamic bindings.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities the component capabilities. Each element is either a unique id, or an {@link ICapability}
	 */
	public AbstractDynamicCapability(final String name, final String description, final Collection<Object> capabilities) {
		this.name = name;
		this.description = description;
		this.capabilities = Sets.newHashSet(capabilities);
	}
	
	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String getDescription() {
		return description;
	}

	/**
	 * Resolve the capability binding for <code>key</code>.
	 * @param key a capability id or {@link ICapability}
	 * @return the capability binding for <code>key</code>
	 * @throws NoSuchCapabilityException if <code>key</code> cannot be resolved
	 */
	public final ICapability get(final Object key) throws NoSuchCapabilityException {
		if (key instanceof String) {
			return current().get((String) key);
		} else if (key instanceof ICapability) {
			return (ICapability) key;
		} else {
			throw new IllegalArgumentException();
		}
	}


	/**
	 * Returns a mapping from unique IDs to resolved capabilities for the component capabilities. 
	 * @return a mapping from unique IDs to resolved capabilities for the component capabilities. 
	 * @throws NoSuchCapabilityException if a dynamic binding cannot be resolved
	 */
	public final Map<String, ICapability> current() throws NoSuchCapabilityException {
		Map<String, ICapability> result = Maps.newHashMap();
		for (Object capability : capabilities) {
			if (capability instanceof ICapability) {
				ICapability x = (ICapability) capability;
				result.put(x.getName(), x);
			} else if (capability instanceof String) {
				result.put((String) capability, CupidPlatform.getCapabilityRegistry().findCapability((String) capability));
			} else {
				throw new RuntimeException("Unexpected component of type " + capability.getClass().getName());
			}
		}
		return result;
	}
	
	@Override
	public final Set<String> getDynamicDependencies() {
		return Sets.newHashSet(Iterables.filter(capabilities, String.class));
	}
}
