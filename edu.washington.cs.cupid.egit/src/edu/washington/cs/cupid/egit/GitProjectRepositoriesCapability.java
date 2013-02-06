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

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.egit.core.project.RepositoryFinder;
import org.eclipse.egit.core.project.RepositoryMapping;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * Searches for existing Git repositories associated with a project's files.
 * @author Todd Schiller
 */
public final class GitProjectRepositoriesCapability extends LinearCapability<IProject, Collection<RepositoryMapping>> {

	/**
	 * Construct a capability that searches for existing Git repositories associated with a project's files.
	 */
	public GitProjectRepositoriesCapability() {
		super("Git Repositories",
			  "edu.washington.cs.cupid.egit.repositories",
			  "Get all Git repositories associated with a project",
			  TypeToken.of(IProject.class), new TypeToken<Collection<RepositoryMapping>>() {},
			  Flag.PURE, Flag.TRANSIENT);	
	}
	
	@Override
	public LinearJob<IProject, Collection<RepositoryMapping>> getJob(final IProject input) {
		return new LinearJob<IProject, Collection<RepositoryMapping>>(this, input) {
			@Override
			protected LinearStatus<Collection<RepositoryMapping>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
					RepositoryFinder finder = new RepositoryFinder(input);
					return LinearStatus.makeOk(getCapability(), finder.find(new SubProgressMonitor(monitor, 100)));
				} catch (Exception e) {
					return LinearStatus.<Collection<RepositoryMapping>>makeError(e);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
