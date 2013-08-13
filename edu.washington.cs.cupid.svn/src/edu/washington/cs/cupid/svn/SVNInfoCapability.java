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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * Capability that returns the SVN information for a working copy resource.
 * @author Todd Schiller
 */
public final class SVNInfoCapability extends LinearCapability<IResource, SVNInfo> {

	/**
	 * Construct a capability that returns the SVN information for a working copy resource.
	 */
	public SVNInfoCapability() {
		super("SVN Head Info",
			  "edu.washington.cs.cupid.svn.info",
			  "SVN information for a resource",
			  IResource.class, SVNInfo.class,
			  Flag.PURE);
	}
	
	@Override
	public LinearJob<IResource, SVNInfo> getJob(final IResource input) {
		return new LinearJob<IResource, SVNInfo>(this, input) {
			@Override
			protected LinearStatus<SVNInfo> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask("SVN Info", 1);
					
					SVNClientManager svn = SVNClientManager.newInstance();
					SVNWCClient wc = svn.getWCClient();
				
					return LinearStatus.makeOk(getCapability(), wc.doInfo(input.getLocation().toFile(), SVNRevision.HEAD));
				} catch (SVNException e) {
					return LinearStatus.<SVNInfo>makeError(e);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
