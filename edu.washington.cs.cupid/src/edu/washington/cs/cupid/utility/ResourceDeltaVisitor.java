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
package edu.washington.cs.cupid.utility;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Sets;

/**
 * Aggregates {@link IResource} subtrees with leaf nodes with specified flags.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class ResourceDeltaVisitor implements IResourceDeltaVisitor {

	private final Set<IResource> matches = Sets.newHashSet();
	private final int flags;
	
	/**
	 * Construct a resource visitor that aggregates nodes matching <code>flags</code>.
	 * @see {@link IResourceDelta#getFlags()}
	 * @param flags the resource delta flags to match
	 */
	public ResourceDeltaVisitor(final int flags) {
		this.flags = flags;
	}
		
	/**
	 * Returns the aggregated resources.
	 * @return the aggregated resources
	 */
	public Set<IResource> getMatches() {
		return matches;
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		if ((delta.getFlags() & flags) != 0) {
			addAll(delta.getResource());
		}
		return true;
	}
	
	private void addAll(final IResource resource) {
		if (resource != null) {
			matches.add(resource);
			addAll(resource.getParent());
		}
	}
}
