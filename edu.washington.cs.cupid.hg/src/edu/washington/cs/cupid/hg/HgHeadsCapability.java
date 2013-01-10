package edu.washington.cs.cupid.hg;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;

public class HgHeadsCapability extends GenericAbstractCapability<IResource, List<ChangeSet>> {

	public HgHeadsCapability() {
		super("Hg Heads",
			  "edu.washington.cs.cupid.hg.heads",
			  "Hg heads for the resource's repository",
			  Flag.PURE, Flag.TRANSIENT);
	}
	
	@Override
	public TypeToken<IResource> getParameterType() {
		return TypeToken.of(IResource.class);
	}

	@SuppressWarnings("serial")
	@Override
	public TypeToken<List<ChangeSet>> getReturnType() {
		return new TypeToken<List<ChangeSet>>(getClass()){};
	}

	@Override
	public CapabilityJob<IResource, List<ChangeSet>> getJob(IResource input) {
		return new CapabilityJob<IResource, List<ChangeSet>>(this, input){
			@Override
			protected CapabilityStatus<List<ChangeSet>> run(IProgressMonitor monitor) {
				monitor.beginTask("Getting Hg Repository Heads", 1);
				try{
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(input);
					
					if (root == null){
						return CapabilityStatus.makeError(new ResourceNotHgVersionedException(input));
					}
					
					List<ChangeSet> result = 
							Lists.newArrayList(HgLogClient.getChangeSets(root,HgLogClient.getHeads(root)));
					return CapabilityStatus.makeOk(result);
				}catch (Exception ex){
					return CapabilityStatus.makeError(ex);
				}finally{
					monitor.done();
				}
			}
		};
	}

}