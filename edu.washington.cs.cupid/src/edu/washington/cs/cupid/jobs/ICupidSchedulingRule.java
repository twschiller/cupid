package edu.washington.cs.cupid.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Cupid job scheduling rule generator.
 * @author Todd Schiller
 * @param <T> the type for which the rule applies
 */
public interface ICupidSchedulingRule<T> {

	/**
	 * Returns the class for which the rule applies.
	 * @return the class for which the rule applies
	 */
	Class<T> getRuleClass();
	
	/**
	 * Returns the scheduling rule for <code>obj</code>.
	 * @param obj an object
	 * @return the scheduling rule for <code>obj</code>.
	 */
	ISchedulingRule getRule(T obj);
	
}
