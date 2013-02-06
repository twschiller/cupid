/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
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
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * Capability that returns the full log of an SVN project.
 * @author Todd Schiller
 */
public final class SVNLogCapability extends LinearCapability<IResource, List<SVNLogEntry>> {

	// http://wiki.svnkit.com/Printing_Out_Repository_History

	/**
	 * Construct a capability that returns the full log of an SVN project.
	 */
	public SVNLogCapability() {
		super("Project SVN Log",
			  "edu.washington.cs.cupid.svn.log",
			  "SVN log entries for the project",
			  TypeToken.of(IResource.class), new TypeToken<List<SVNLogEntry>>(){},
			  Flag.PURE, Flag.TRANSIENT);
	}

	@Override
	public LinearJob<IResource, List<SVNLogEntry>> getJob(final IResource input) {

		return new LinearJob<IResource, List<SVNLogEntry>>(this, input) {
			@Override
			protected LinearStatus<List<SVNLogEntry>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
					
					SVNClientManager svn = SVNClientManager.newInstance();
					SVNLogClient log = svn.getLogClient();
					SVNWCClient workingCopy = svn.getWCClient();

					// local working copy information
					SVNInfo info = workingCopy.doInfo(input.getLocation().toFile(), SVNRevision.HEAD);

					final List<SVNLogEntry> entries = Lists.newArrayList();

					log.doLog(info.getURL(), new String[]{""}, SVNRevision.create(0), SVNRevision.create(0), SVNRevision.HEAD, true, false, -1, new ISVNLogEntryHandler() {
						@Override
						public void handleLogEntry(final SVNLogEntry entry) throws SVNException {
							entries.add(entry);
						}
					});
				
					return LinearStatus.makeOk(getCapability(), entries);
				} catch (Exception e) {
					return LinearStatus.<List<SVNLogEntry>>makeError(e);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
