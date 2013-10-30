package edu.washington.cs.cupid.egit;

import java.util.Calendar;

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

public class GitWasModifiedCapability extends AbstractBaseCapability {

	public static final IParameter<IResource> PARAM_RESOURCE = new Parameter<IResource>("Resource", IResource.class);
	public static final IParameter<String> PARAM_REVISION = new OptionalParameter<String>("Revision", String.class, "");
	public static final IParameter<Integer> DAYS_AGO = new OptionalParameter<Integer>("Within X Days", Integer.class, -1);
	public static final Output<Boolean> OUT_MODIFIED = new Output<Boolean>("Modified?", TypeToken.of(Boolean.class));
	
	/**
	 * Construct a capability that returns <tt>true</tt> if the given resource was modified in the
	 * given revision.
	 */
	public GitWasModifiedCapability() {
		super("Git Modified?",
			  "True if the resource was modified in the specified revision",
			  Lists.<IParameter<?>>newArrayList(PARAM_RESOURCE, DAYS_AGO, PARAM_REVISION),
			  Lists.<Output<?>>newArrayList(OUT_MODIFIED),
			  Flag.PURE);
	}
	
	@Override
	public CapabilityJob<GitWasModifiedCapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<GitWasModifiedCapability>(this, input) {
			@Override
			protected CapabilityStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 2);
					
					IResource resource = input.getValueArgument(PARAM_RESOURCE);
					String revision = input.getValueArgument(PARAM_REVISION);
					Integer withinDays = input.getValueArgument(DAYS_AGO);
					
					if (revision.isEmpty() && withinDays <= 0){
						throw new IllegalArgumentException("Git Modified?: Must specify revision or time period");
					}
					
					GitFileHistoryProvider provider = new GitFileHistoryProvider();
					IFileHistory history = provider.getFileHistoryFor(resource, IFileHistoryProvider.SINGLE_LINE_OF_DESCENT, new SubProgressMonitor(monitor, 1));
					
					boolean found = false;
					
					Calendar startDate = Calendar.getInstance();
					startDate.add(Calendar.DATE, -withinDays);
					
					for (IFileRevision r : history.getFileRevisions()){
						Calendar rt =  Calendar.getInstance();
						rt.setTimeInMillis(r.getTimestamp());
						
						if (!revision.isEmpty() && revision.equals(r.getContentIdentifier())){
							found = true;
							break;
						}else if (withinDays > 0 && rt.after(startDate)){
							found = true;
							break;
						}
					}
					
					return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(OUT_MODIFIED, found));
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
