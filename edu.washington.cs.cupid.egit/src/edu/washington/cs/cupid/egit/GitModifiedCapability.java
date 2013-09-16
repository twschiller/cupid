package edu.washington.cs.cupid.egit;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.egit.core.internal.storage.GitFileHistoryProvider;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractBaseCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.OptionalParameter;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.Parameter;

public class GitModifiedCapability extends AbstractBaseCapability {

	public static final IParameter<IResource> PARAM_RESOURCE = new Parameter<IResource>("Resource", IResource.class);
	public static final IParameter<String> PARAM_REVISION = new OptionalParameter<String>("Revision", String.class, "");
	public static final Output<Boolean> OUT_MODIFIED = new Output<Boolean>("Modified?", TypeToken.of(Boolean.class));
	
	/**
	 * Construct a capability that returns <tt>true</tt> if the given resource was modified in the
	 * given revision.
	 */
	public GitModifiedCapability() {
		super("Git Modified",
			  "True if the resource was modified in the specified revision",
			  Lists.<IParameter<?>>newArrayList(PARAM_RESOURCE, PARAM_REVISION),
			  Lists.<Output<?>>newArrayList(OUT_MODIFIED),
			  Flag.PURE);
	}
	
	@Override
	public CapabilityJob<GitModifiedCapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<GitModifiedCapability>(this, input) {
			@Override
			protected CapabilityStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 2);
					
					IResource resource = input.getValueArgument(PARAM_RESOURCE);
					String revision = input.getValueArgument(PARAM_REVISION);
					
					if (revision.equals("")){
						throw new IllegalArgumentException("Must specify a Git revision id");
					}
					
					GitFileHistoryProvider provider = new GitFileHistoryProvider();
					IFileHistory history = provider.getFileHistoryFor(resource, IFileHistoryProvider.SINGLE_LINE_OF_DESCENT, new SubProgressMonitor(monitor, 1));
					
					IFileRevision fRevision = history.getFileRevision(revision);
					
					return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(OUT_MODIFIED, fRevision != null));
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
