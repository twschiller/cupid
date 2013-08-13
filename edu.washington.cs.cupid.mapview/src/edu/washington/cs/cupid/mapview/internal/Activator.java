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
package edu.washington.cs.cupid.mapview.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Activator for the Cupid Map View plug-in.
 * @author Todd Schiller
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The Cupid Map View plug-in ID.
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.mapview"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static ILog pluginLog;
	
	/**
	 * The constructor.
	 */
	public Activator() {
	}

	@Override
	public final void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pluginLog = Platform.getLog(context.getBundle());
	}

	@Override
	public final void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Log an error in the plugin's log.
	 * @param msg localized error message
	 * @param e the exception
	 */
	public void logError(final String msg, final Exception e) {
		pluginLog.log(new Status(Status.ERROR, PLUGIN_ID, Status.ERROR, msg, e));			
	}

}
