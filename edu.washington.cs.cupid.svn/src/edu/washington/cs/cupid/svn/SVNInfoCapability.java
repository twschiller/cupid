package edu.washington.cs.cupid.svn;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

/**
 * Capability that returns the SVN information for a working copy resource
 * @author Todd Schiller
 */
public class SVNInfoCapability extends AbstractCapability<IResource, SVNInfo>{

	public SVNInfoCapability() {
		super("SVN Info",
			  "edu.washington.cs.cupid.svn.info",
			  "SVN information for a resource",
			  IResource.class, SVNInfo.class,
			  Flag.PURE);
	}
	
	@Override
	public CapabilityJob<IResource, SVNInfo> getJob(IResource input) {
		return new CapabilityJob<IResource, SVNInfo>(this, input){
			@Override
			protected CapabilityStatus<SVNInfo> run(IProgressMonitor monitor) {
				SVNClientManager svn = SVNClientManager.newInstance();
				SVNWCClient wc = svn.getWCClient();
				
				try {
					return CapabilityStatus.makeOk(wc.doInfo(input.getLocation().toFile(), SVNRevision.HEAD));
				} catch (SVNException e) {
					return CapabilityStatus.makeError(e);
				}
			}
		};
	}


}
