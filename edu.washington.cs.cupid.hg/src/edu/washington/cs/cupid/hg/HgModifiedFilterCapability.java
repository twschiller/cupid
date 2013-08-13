package edu.washington.cs.cupid.hg;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import edu.washington.cs.cupid.capability.AbstractBaseCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.OptionalParameter;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.Parameter;

public class HgModifiedFilterCapability extends AbstractBaseCapability {

	public static final IParameter<Collection<IResource>> PARAM_RESOURCE = new Parameter<Collection<IResource>>("Resource", new TypeToken<Collection<IResource>>(){});
	public static final IParameter<Integer> PARAM_REVISION = new OptionalParameter<Integer>("Revision", Integer.class, -1);
	public static final Output<Collection<IResource>> OUT_MODIFIED = new Output<Collection<IResource>>("Modified", new TypeToken<Collection<IResource>>(){});

	/**
	 * Construct a capability that returns <tt>true</tt> if the given resource was modified in the
	 * given revision.
	 */
	public HgModifiedFilterCapability() {
		super("Hg Modified (Filter)",
			  "Returns the resources modified in the specified revision (or last revision)",
			  Lists.<IParameter<?>>newArrayList(PARAM_RESOURCE, PARAM_REVISION),
			  Lists.<Output<?>>newArrayList(OUT_MODIFIED),
			  Flag.PURE);
	}


	@Override
	public CapabilityJob<AbstractBaseCapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<AbstractBaseCapability> (this, input){
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try{
					Collection<IResource> resources = input.getValueArgument(PARAM_RESOURCE);
					int revision = input.getValueArgument(PARAM_REVISION);
					
					List<IResource> result = Lists.newArrayList();
					
					monitor.beginTask(getName(), resources.size());
					
					for (IResource r : resources){
						HgRoot root = MercurialRootCache.getInstance().getHgRoot(r);
							
						if (root != null){
							ChangeSet changeSet = revision < 0 ? 
									HgLogClient.getChangeSet(root, HgLogClient.getCurrentChangeset(root)) :
									HgLogClient.getChangeSet(root, revision);
						
							if (changeSet.contains(r)){
								result.add(r);
							}
						}
						
						monitor.worked(1);
					}
					
					return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(OUT_MODIFIED, result));
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}finally{
					monitor.done();
				}
			}
		};
	}
	
}
