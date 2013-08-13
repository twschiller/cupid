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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Locates a resource within a project.
 * @author Todd Schiller
 */
public final class ResourceLocator implements IResourceVisitor {
	private IResource result;
	private IPath query;
	
	private ResourceLocator(final IPath query) {
		this.query = query;
		this.result = null;
	}
	
	@Override
	public boolean visit(final IResource resource) throws CoreException {
		if (resource.getProjectRelativePath().equals(query)) {
			result = resource;
			return false;
		}
		return result == null;
	}
	
	/**
	 * Returns the {@link IResource} with path <code>query</code> in <code>project</code>. 
	 * @param project the project
	 * @param query the query path
	 * @return the {@link IResource} with path <code>query</code> in <code>project</code>
	 */
	public static IResource find(final IProject project, final IPath query) {
		ResourceLocator visitor = new ResourceLocator(query);
		try {
			project.accept(visitor, IResource.DEPTH_INFINITE, true);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return visitor.result;
	}	
}
