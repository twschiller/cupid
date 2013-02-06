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
package edu.washington.cs.cupid.egit;

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

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * A capability that returns the Git history from a single line of descent (i.e., branch) for
 * a resource.
 * @author Todd Schiller
 */
public final class GitHistoryCapability extends LinearCapability<IResource, List<IFileRevision>> {

	/**
	 * Constructs capability that returns the Git history from a single line of descent (i.e., branch) for
	 * a resource.
	 */
	public GitHistoryCapability() {
		super("Git History",
			  "edu.washington.cs.cupid.egit.history",
			  "Git log entries for the resource",
			  TypeToken.of(IResource.class), new TypeToken<List<IFileRevision>>() {},
			  Flag.PURE, Flag.TRANSIENT);
	}

	@Override
	public LinearJob<IResource, List<IFileRevision>> getJob(final IResource input) {
		return new LinearJob<IResource, List<IFileRevision>>(this, input) {
			@Override
			protected LinearStatus<List<IFileRevision>> run(final IProgressMonitor monitor) {
				
				try {
					monitor.beginTask(getName(), 100);
					
					GitFileHistoryProvider provider = new GitFileHistoryProvider();
					IFileHistory history = provider.getFileHistoryFor(input, IFileHistoryProvider.SINGLE_LINE_OF_DESCENT, new SubProgressMonitor(monitor, 100));
					
					List<IFileRevision> revisions = Lists.newArrayList(history.getFileRevisions());
					
					return LinearStatus.makeOk(getCapability(), revisions);
				} catch (Exception ex) {
					return LinearStatus.<List<IFileRevision>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
