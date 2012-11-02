package edu.washington.cs.cupid.svn;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

/**
 * Capability that returns the full log of an SVN project
 * @author Todd Schiller
 */
public class SVNLogCapability extends AbstractCapability<IResource, List<SVNLogEntry>>{

	// http://wiki.svnkit.com/Printing_Out_Repository_History

	public SVNLogCapability() {
		super("SVN Log",
			  "edu.washington.cs.cupid.svn.log",
			  "SVN log entries for the project",
			  TypeToken.of(IResource.class), Types.SVN_LOG,
			  Flag.PURE, Flag.TRANSIENT);
	}

	@Override
	public CapabilityJob<IResource, List<SVNLogEntry>> getJob(IResource input) {

		return new CapabilityJob<IResource, List<SVNLogEntry>>(this, input){
			@Override
			protected CapabilityStatus<List<SVNLogEntry>> run(IProgressMonitor monitor) {
				try{

					SVNClientManager svn = SVNClientManager.newInstance();
					SVNLogClient log = svn.getLogClient();
					SVNWCClient workingCopy = svn.getWCClient();

					// local working copy information
					SVNInfo info = null;

					try {
						info = workingCopy.doInfo(input.getLocation().toFile(), SVNRevision.HEAD);
					} catch (SVNException e) {
						CapabilityStatus.makeError(e);
					}

					final List<SVNLogEntry> entries = Lists.newArrayList();

					try {
						log.doLog(info.getURL(), new String[]{""}, SVNRevision.create(0), SVNRevision.create(0), SVNRevision.HEAD, true, false, -1, new ISVNLogEntryHandler(){
							@Override
							public void handleLogEntry(SVNLogEntry entry) throws SVNException {
								entries.add(entry);
							}
						});
					} catch (SVNException e) {
						return CapabilityStatus.makeError(e);
					}

					return CapabilityStatus.makeOk(entries);
				}catch(Exception e){
					return CapabilityStatus.makeError(e);
				}
			}
		};
	}
}
