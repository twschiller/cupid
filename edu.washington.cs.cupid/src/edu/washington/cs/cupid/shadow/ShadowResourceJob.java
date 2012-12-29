package edu.washington.cs.cupid.shadow;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


/**
 * A {@link Job} that produces a reference to the {@link IResource} in the corresponding
 * shadow project.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class ShadowResourceJob extends Job implements IShadowJob<IResource>{

	private final IResource resource;
	protected IResource shadow = null;
	
	public ShadowResourceJob(IResource resource) {
		super("shadow resource " + resource.getName());
		this.resource = resource;
	}
	
	@Override
	public IResource get() {
		return shadow;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (resource instanceof IProject){
			shadow = ShadowProjectManager.createShadowProject((IProject) resource);
		}else{
			IProject project = ShadowProjectManager.createShadowProject(resource.getProject());
			shadow = ResourceLocator.find(project, resource.getProjectRelativePath());
		}
		
		return Status.OK_STATUS;
	}
}
