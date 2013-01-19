package edu.washington.cs.cupid.egit;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.project.RepositoryFinder;
import org.eclipse.egit.core.project.RepositoryMapping;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

/**
 * Searches for existing Git repositories associated with a project's files.
 * @author Todd Schiller
 */
public final class GitProjectRepositoriesCapability extends AbstractCapability<IProject, Collection<RepositoryMapping>> {

	/**
	 * Construct a capability that searches for existing Git repositories associated with a project's files.
	 */
	public GitProjectRepositoriesCapability() {
		super("Git Repositories",
			  "edu.washington.cs.cupid.egit.repositories",
			  "Get all Git repositories associated with a project",
			  TypeToken.of(IProject.class), new TypeToken<Collection<RepositoryMapping>>() {},
			  Flag.PURE, Flag.TRANSIENT);	
	}
	
	@Override
	public CapabilityJob<IProject, Collection<RepositoryMapping>> getJob(final IProject input) {
		return new CapabilityJob<IProject, Collection<RepositoryMapping>>(this, input) {
			@Override
			protected CapabilityStatus<Collection<RepositoryMapping>> run(final IProgressMonitor monitor) {
				RepositoryFinder finder = new RepositoryFinder(input);
				try {
					return CapabilityStatus.makeOk(finder.find(monitor));
				} catch (Exception e) {
					return CapabilityStatus.makeError(e);
				}
			}
		};
	}

}
