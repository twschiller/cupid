package edu.washington.cs.cupid.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class ProjectForResourceCapability extends AbstractCapability<IResource, IProject>{

	public ProjectForResourceCapability(){
		super(
				"Containing Project",
				"edu.washington.cs.cupid.resources.project",
				"The project that contains the resource",
				IResource.class, IProject.class,
				Flag.PURE);
	}

	@Override
	public CapabilityJob<IResource, IProject> getJob(IResource input) {
		return new CapabilityJob<IResource, IProject>(this, input){
			@Override
			protected CapabilityStatus<IProject> run(IProgressMonitor monitor) {
				return CapabilityStatus.makeOk(input.getProject());
			}
		};
	}
}
