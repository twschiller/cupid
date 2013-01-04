package edu.washington.cs.cupid.jdt.scheduling;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaElement;

import edu.washington.cs.cupid.jobs.ICupidSchedulingRule;

public class JavaSchedulingRule implements ICupidSchedulingRule<IJavaElement>{

	@Override
	public Class<IJavaElement> getRuleClass() {
		return IJavaElement.class;
	}

	@Override
	public ISchedulingRule getRule(IJavaElement obj) {
		return obj.getSchedulingRule();
	}
}
