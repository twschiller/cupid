package edu.washington.cs.cupid.hg;

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

public class HgModifiedCapability extends AbstractBaseCapability {

	public static final IParameter<IResource> PARAM_RESOURCE = new Parameter<IResource>("Resource", IResource.class);
	public static final IParameter<Integer> PARAM_REVISION = new OptionalParameter<Integer>("Revision", Integer.class, -1);
	public static final Output<Boolean> OUT_MODIFIED = new Output<Boolean>("Modified?", TypeToken.of(Boolean.class));

	/**
	 * Construct a capability that returns <tt>true</tt> if the given resource was modified in the
	 * given revision.
	 */
	public HgModifiedCapability() {
		super("Hg Modified",
			  "True if the resource was modified in the specified revision (or last revision)",
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
					IResource resource = input.getValueArgument(PARAM_RESOURCE);
					int revision = input.getValueArgument(PARAM_REVISION);
					
					monitor.beginTask("Hg Modified? " + resource.getName(), 1);
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(resource);
					
					if (root == null){
						return CapabilityStatus.makeError(new ResourceNotHgVersionedException(resource));
					}
					
					ChangeSet changeSet = revision < 0 ? 
							HgLogClient.getChangeSet(root, HgLogClient.getCurrentChangeset(root))
							: HgLogClient.getChangeSet(root, revision);

					boolean result = changeSet.contains(resource);
					
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
