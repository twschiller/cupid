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
