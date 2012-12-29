package edu.washington.cs.cupid.internal;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Status for jobs spawned internally by the Cupid runtime
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class CupidJobStatus extends Status {

	private final String jobName;
	private final int jobHashCode;
	
	public CupidJobStatus(Job job, int severity, String message) {
		this (job, severity, message, null);
	}
	
	public CupidJobStatus(Job job, int severity, String message, Exception ex) {
		super(severity, CupidActivator.PLUGIN_ID, job.getName() + "\t#" + job.hashCode() + "\t" + message, ex);
		jobName = job.getName();
		jobHashCode = job.hashCode();
	}

	public String getJobName() {
		return jobName;
	}

	public int getJobHashCode() {
		return jobHashCode;
	}
}
