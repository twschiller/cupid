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
package edu.washington.cs.cupid.mylyn.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.mylyn.ActiveContextCapability;
import edu.washington.cs.cupid.mylyn.InActiveContextCapability;
import edu.washington.cs.cupid.mylyn.MylynTaskCapability;
import edu.washington.cs.cupid.mylyn.TaskCapability;
import edu.washington.cs.cupid.mylyn.TaskCommentsCapability;
import edu.washington.cs.cupid.mylyn.TaskContextCapability;
import edu.washington.cs.cupid.mylyn.TaskFilter;
import edu.washington.cs.cupid.mylyn.TasksForResource;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements ICapabilityPublisher{

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.mylyn"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void addChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

	@Override
	public void removeChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

	@Override
	public ICapability[] publish() {
		return new ICapability[]{
			new TaskCapability(),
			new TaskCommentsCapability(),
			new MylynTaskCapability(),
			new TaskContextCapability(),
			new ActiveContextCapability(),
			new InActiveContextCapability(),
			new TasksForResource(),
			new TaskFilter(),
		};
	}
}
