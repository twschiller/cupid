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
package edu.washington.cs.cupid.tests;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;

public class Activator implements BundleActivator, ICapabilityPublisher {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	@Override
	public ICapability[] publish() {
		return new ICapability [] { 
				new ExceptionCapability(), 
				new LongRunningCapability(), 
//				new MethodCapabilityTest(),
				new MultipleOutputTest(),
				//new LongList(),
				//new LongArray(),
				new MultiInputTest(),
				new ClassTest(),
				SnippetTest.RETURNS_TRUE,
		};
	}

	@Override
	public void addChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

	@Override
	public void removeChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

}
