package edu.washington.cs.cupid.egit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.egit.core.internal.storage.GitFileHistoryProvider;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class LastProjectRevision extends LinearCapability<IResource, String> {

	public LastProjectRevision() {
		super("Git Project Revision", "Returns the current Git revision ID for the containing project",
			  IResource.class, String.class,
			  ICapability.Flag.PURE);			
	}

	@Override
	public LinearJob<IResource, String> getJob(IResource input) {
		return new LinearJob<IResource, String>(this, input) {
			@Override
			protected LinearStatus<String> run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
				
					IResource resource = getInput();
					IProject project = resource.getProject();
					
					GitFileHistoryProvider provider = new GitFileHistoryProvider();
					IFileHistory history = provider.getFileHistoryFor(project, IFileHistoryProvider.SINGLE_REVISION, new SubProgressMonitor(monitor, 1));
					
					if (history.getFileRevisions().length > 0){
						return LinearStatus.makeOk(getCapability(),  history.getFileRevisions()[0].getContentIdentifier());
					}else{
						throw new RuntimeException("Project " + project.getName() + " is not under Git version control");
					}
				} catch (Exception ex) {
					return LinearStatus.<String>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
