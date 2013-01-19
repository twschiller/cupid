package edu.washington.cs.cupid.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A registry of job scheduling rules.
 * @see ICupidSchedulingRule
 * @author Todd Schiller
 */
public interface ISchedulingRuleRegistry {

	/**
	 * Add a rule to the scheduling rule registry.
	 * @param rule the rule to register
	 */
	void registerSchedulingRule(final ICupidSchedulingRule<?> rule);
	
	/**
	 * Returns the scheduling rule for <code>obj</code>.
	 * @param obj an object
	 * @return the scheduling rule for <code>obj</code>
	 */
	ISchedulingRule getSchedulingRule(final Object obj);
	
}
