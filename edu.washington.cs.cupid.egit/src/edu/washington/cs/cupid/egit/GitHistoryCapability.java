package edu.washington.cs.cupid.egit;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.internal.storage.GitFileHistoryProvider;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

/**
 * A capability that returns the Git history from a single line of descent (i.e., branch) for
 * a resource.
 * @author Todd Schiller
 */
public final class GitHistoryCapability extends AbstractCapability<IResource, List<IFileRevision>> {

	/**
	 * Constructs capability that returns the Git history from a single line of descent (i.e., branch) for
	 * a resource.
	 */
	public GitHistoryCapability() {
		super("Git History",
			  "edu.washington.cs.cupid.egit.history",
			  "Git log entries for the resource",
			  TypeToken.of(IResource.class), new TypeToken<List<IFileRevision>>() {},
			  Flag.PURE, Flag.TRANSIENT);
	}

	@Override
	public CapabilityJob<IResource, List<IFileRevision>> getJob(final IResource input) {
		return new CapabilityJob<IResource, List<IFileRevision>>(this, input) {
			@Override
			protected CapabilityStatus<List<IFileRevision>> run(final IProgressMonitor monitor) {
				GitFileHistoryProvider provider = new GitFileHistoryProvider();
				IFileHistory history = provider.getFileHistoryFor(input, IFileHistoryProvider.SINGLE_LINE_OF_DESCENT, monitor);
				
				try {
					List<IFileRevision> revisions = Lists.newArrayList(history.getFileRevisions());
					return CapabilityStatus.makeOk(revisions);
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}
}
