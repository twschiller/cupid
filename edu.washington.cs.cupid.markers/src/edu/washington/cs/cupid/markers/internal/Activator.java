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
package edu.washington.cs.cupid.markers.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator for the Cupid marker plug-in.
 */
public final class Activator extends AbstractUIPlugin {

	/**
	 *  The plug-in ID for the Cupid marker plug-in.
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.markers"; //$NON-NLS-1$

	private static Activator plugin;
	
	/**
	 * Constructs the activator for the Cupid marker plug-in.
	 */
	public Activator() {
	}

	private MarkerManager manager;
	
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		manager = new MarkerManager();
		manager.start();
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		manager.stop();
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
