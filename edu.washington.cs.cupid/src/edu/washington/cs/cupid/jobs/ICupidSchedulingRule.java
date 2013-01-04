package edu.washington.cs.cupid.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public interface ICupidSchedulingRule<T> {

	public Class<T> getRuleClass();
	public ISchedulingRule getRule(T obj);
	
}
