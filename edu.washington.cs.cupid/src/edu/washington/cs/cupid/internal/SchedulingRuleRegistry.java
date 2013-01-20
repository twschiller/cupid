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

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.washington.cs.cupid.jobs.ICupidSchedulingRule;
import edu.washington.cs.cupid.jobs.ISchedulingRuleRegistry;

/**
 * The default rule registry.
 * @author Todd Schiller
 */
public final class SchedulingRuleRegistry implements ISchedulingRuleRegistry {

	@SuppressWarnings("rawtypes")
	private Map<Class, ICupidSchedulingRule> registry = Maps.newHashMap();

	/**
	 * A scheduling rule that does not conflict with other rules.
	 * @author Todd Schiller
	 */
	public static class NullSchedulingRule implements ISchedulingRule {
		@Override
		public final boolean contains(final ISchedulingRule rule) {
			return false;
		}

		@Override
		public final boolean isConflicting(final ISchedulingRule rule) {
			return false;
		}
	}
	
	@Override
	public void registerSchedulingRule(final ICupidSchedulingRule<?> rule) {
		registry.put(rule.getRuleClass(), rule);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ISchedulingRule getSchedulingRule(final Object object) {
		Class<?> rt = object.getClass();
		
		if (object instanceof ISchedulingRule) {
			return (ISchedulingRule) object;
		} else {
			List<ISchedulingRule> rules = Lists.newArrayList();
			
			for (Map.Entry<Class, ICupidSchedulingRule> rule : registry.entrySet()) {
				if (rule.getKey().isAssignableFrom(rt)) {
					rules.add(rule.getValue().getRule(object));
				}
			}
	
			return rules.isEmpty()
				? new NullSchedulingRule()
				: new MultiRule(rules.toArray(new ISchedulingRule[]{}));
		}
	}

}
