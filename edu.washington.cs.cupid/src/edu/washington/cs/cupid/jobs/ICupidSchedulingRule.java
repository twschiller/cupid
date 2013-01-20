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
