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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * An Eclipse capability (i.e., service).
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <V> the output type
 */
public abstract class AbstractCapability implements ICapability {

	private final String name;
	private final String uniqueId;
	private final String description;
	private final EnumSet<Flag> flags;
	
	/**
	 * A standard capability.
	 * @param name capability name
	 * @param uniqueId capability unique id
	 * @param description capability description
	 * @param flags capability property flags
	 */
	public AbstractCapability(
			final String name, final String uniqueId, final String description,
			final Flag... flags) {
		
		this.name = name;
		this.uniqueId = uniqueId;
		this.description = description;
	
		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
	}

	@Override
	public final String getUniqueId() {
		return uniqueId;
	}

	@Override
	public final String getName() {
		return name;
	}
	
	public EnumSet<Flag> getFlags() {
		return flags;
	}

	@Override
	public final String getDescription() {
		return description;
	}

	@Override
	public final String toString() {
		return getUniqueId();
	}

}
