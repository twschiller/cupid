package edu.washington.cs.cupid.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public interface ISchedulingRuleRegistry {

	public void registerSchedulingRule(ICupidSchedulingRule<?> rule);
	public ISchedulingRule getSchedulingRule(Object type);
	
}
