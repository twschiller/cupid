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
package edu.washington.cs.cupid.internal;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Status for jobs spawned internally by the Cupid runtime. Does not store a reference to 
 * the job.
 * @see {@link Job}
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class CupidJobStatus extends Status {

	private final String jobName;
	private final int jobHashCode;
	
	/**
	 * Construct a status for <code>job</code>.
	 * @param job the job
	 * @param severity the status severity
	 * @param message the status message
	 */
	public CupidJobStatus(final Job job, final int severity, final String message) {
		this (job, severity, message, null);
	}
	
	/**
	 * Construct an exceptional status for <code>job</code>.
	 * @param job the job
	 * @param severity the status severity
	 * @param message the status message
	 * @param ex the exception
	 */
	public CupidJobStatus(final Job job, final int severity, final String message, final Exception ex) {
		super(severity, CupidActivator.PLUGIN_ID, job.getName() + "\t#" + job.hashCode() + "\t" + message, ex);
		jobName = job.getName();
		jobHashCode = job.hashCode();
	}

	/**
	 * Returns the name of associated job.
	 * @return  the name of associated job
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * Returns the hashcode of the associated job.
	 * @return the hashcode of the associated job
	 */
	public int getJobHashCode() {
		return jobHashCode;
	}
}
