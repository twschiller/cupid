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

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

/**
 * A capability that returns the project that contains a resource.
 * @author Todd Schiller
 */
public final class ProjectForResourceCapability extends AbstractCapability<IResource, IProject> {

	/**
	 * Construct a capability that returns the project that contains a resource.
	 */
	public ProjectForResourceCapability() {
		super(
				"Containing Project",
				"edu.washington.cs.cupid.resources.project",
				"The project that contains the resource",
				IResource.class, IProject.class,
				Flag.PURE);
	}

	@Override
	public CapabilityJob<IResource, IProject> getJob(final IResource input) {
		return new CapabilityJob<IResource, IProject>(this, input) {
			@Override
			protected CapabilityStatus<IProject> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					return CapabilityStatus.makeOk(input.getProject());
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
