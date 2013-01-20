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
