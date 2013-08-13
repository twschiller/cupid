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

import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * Capability that returns a file's last modified date.
 * @author Todd Schiller
 */
public final class LastModifiedCapability extends LinearCapability<IFile, Date> {

	/**
	 * Construct a capability that returns a file's last modified date.
	 */
	public LastModifiedCapability() {
		super(
				"Last Modified",
				"The file's last modified date",
				IFile.class, Date.class,
				Flag.PURE);
	}

	@Override
	public LinearJob<IFile, Date> getJob(final IFile input) {
		return new LinearJob<IFile, Date>(this, input) {
			@Override
			protected LinearStatus<Date> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					return LinearStatus.makeOk(getCapability(), new Date(getInput().getLocalTimeStamp()));
				} catch (Exception ex) {
					return LinearStatus.<Date>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
