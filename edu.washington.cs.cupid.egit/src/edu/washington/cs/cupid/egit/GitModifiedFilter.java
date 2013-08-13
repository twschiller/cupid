package edu.washington.cs.cupid.egit;

import java.util.Collection;
import java.util.List;

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

public class GitModifiedFilter extends AbstractBaseCapability {

	public static final IParameter<Collection<IResource>> PARAM_RESOURCE = new Parameter<Collection<IResource>>("Resource", new TypeToken<Collection<IResource>>(){});
	public static final IParameter<String> PARAM_REVISION = new OptionalParameter<String>("Revision", String.class, "");
	public static final Output<Collection<IResource>> OUT_MODIFIED = new Output<Collection<IResource>>("Modified", new TypeToken<Collection<IResource>>(){});
	
	/**
	 * Construct a capability that returns <tt>true</tt> if the given resource was modified in the
	 * given revision.
	 */
	public GitModifiedFilter() {
		super("Git Modified (Filter)",
			  "Returns the resources modified in the specified revision",
			  Lists.<IParameter<?>>newArrayList(PARAM_RESOURCE, PARAM_REVISION),
			  Lists.<Output<?>>newArrayList(OUT_MODIFIED),
			  Flag.PURE);
	}
	
	@Override
	public CapabilityJob<GitModifiedFilter> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<GitModifiedFilter>(this, input) {
			@Override
			protected CapabilityStatus run(IProgressMonitor monitor) {
				try {
					Collection<IResource> resources = input.getValueArgument(PARAM_RESOURCE);
					
					monitor.beginTask(getName(), resources.size());
					
					String revision = input.getValueArgument(PARAM_REVISION);
					
					if (revision.equals("")){
						throw new IllegalArgumentException("Must specify a Git revision id");
					}
					
					GitFileHistoryProvider provider = new GitFileHistoryProvider();
					
					List<IResource> result = Lists.newArrayList();
					
					for (IResource resource : resources){
						IFileHistory history = provider.getFileHistoryFor(resource, IFileHistoryProvider.SINGLE_LINE_OF_DESCENT, new SubProgressMonitor(monitor, 100));
						IFileRevision fRevision = history.getFileRevision(revision);
						
						if (fRevision != null){
							result.add(resource);
						}
					}
							
					return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(OUT_MODIFIED, result));
					
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
