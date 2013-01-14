package edu.washington.cs.cupid.shadow;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Locates a resource within a project
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
