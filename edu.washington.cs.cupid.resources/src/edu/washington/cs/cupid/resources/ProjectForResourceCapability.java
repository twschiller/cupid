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
package edu.washington.cs.cupid.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * A capability that returns the project that contains a resource.
 * @author Todd Schiller
 */
public final class ProjectForResourceCapability extends LinearCapability<IResource, IProject> {

	/**
	 * Construct a capability that returns the project that contains a resource.
	 */
	public ProjectForResourceCapability() {
		super(
				"Containing Project",
				"The project that contains the resource",
				IResource.class, IProject.class,
				Flag.PURE);
	}

	@Override
	public LinearJob<IResource, IProject> getJob(final IResource input) {
		return new LinearJob<IResource, IProject>(this, input) {
			@Override
			protected LinearStatus<IProject> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					return LinearStatus.makeOk(getCapability(), input.getProject());
				} catch (Exception ex) {
					return LinearStatus.<IProject>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
