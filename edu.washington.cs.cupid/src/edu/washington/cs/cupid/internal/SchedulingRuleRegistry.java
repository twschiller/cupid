package edu.washington.cs.cupid.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.washington.cs.cupid.jobs.ICupidSchedulingRule;
import edu.washington.cs.cupid.jobs.ISchedulingRuleRegistry;

public class SchedulingRuleRegistry implements ISchedulingRuleRegistry{

	private Map<Class, ICupidSchedulingRule> registry = Maps.newHashMap();

	public static class NullSchedulingRule implements ISchedulingRule{
		@Override
		public boolean contains(ISchedulingRule rule) {
			return false;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return false;
		}
	}
	
	@Override
	public void registerSchedulingRule(ICupidSchedulingRule<?> rule) {
		registry.put(rule.getRuleClass(), rule);
	}

	@Override
	public ISchedulingRule getSchedulingRule(Object object) {
		Class<?> rt = object.getClass();
		
		if (object instanceof ISchedulingRule){
			return (ISchedulingRule) object;
		}else{
			List<ISchedulingRule> rules = Lists.newArrayList();
			
			for (Map.Entry<Class, ICupidSchedulingRule> rule : registry.entrySet()){
				if (rule.getKey().isAssignableFrom(rt)){
					rules.add(rule.getValue().getRule(object));
				}
			}
	
			return rules.isEmpty()
				? new NullSchedulingRule()
				: new MultiRule(rules.toArray(new ISchedulingRule[]{}));
		}
	}

}
