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
