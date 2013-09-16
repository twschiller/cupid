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
package edu.washington.cs.cupid.hg;

import org.eclipse.core.resources.IResource;

public class ResourceNotHgVersionedException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	private final IResource resource;

	public ResourceNotHgVersionedException(IResource resource) {
		super("Resource " + resource.getName() + " not versioned with Mercurial");
		this.resource = resource;
	}

	public IResource getResource() {
		return resource;
	}

}
