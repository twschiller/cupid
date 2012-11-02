package edu.washington.cs.cupid.shadow;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ResourceLocator implements IResourceVisitor{
	private IResource result;
	private IPath query;
	
	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (resource.getProjectRelativePath().equals(query)){
			result = resource;
			return false;
		}
		return result == null;
	}
	
	public static IResource find(IProject project, IPath query){
		ResourceLocator visitor = new ResourceLocator();
		try {
			project.accept(visitor, IResource.DEPTH_INFINITE, true);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return visitor.result;
	}	
}
