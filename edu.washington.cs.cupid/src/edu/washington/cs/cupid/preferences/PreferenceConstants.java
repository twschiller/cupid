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
package edu.washington.cs.cupid.preferences;

/**
 * Constant definitions for plug-in preferences.
 */
public final class PreferenceConstants {
	
	private PreferenceConstants() {
		// NO OP
	}
	
	/**
	 * Enable impure pipelines.
	 */
	public static final String P_IMPURE = "impurePreference";
	
	/**
	 * File-system directory where user-defined capabilities are stored.
	 */
	public static final String P_ARROW_DIR = "cupidArrowDirectory";
	
	/**
	 * Enable capability job status logging.
	 */
	public static final String P_JOB_STATUS_LOGGING = "jobStatusLoggingPreference";
	
	/**
	 * Enable capability cache event logging.
	 */
	public static final String P_CACHE_STATUS_LOGGING = "cacheStatusLoggingPreference";
	
	/**
	 * Time to wait before killing jobs spawned by the selection inspector.
	 */
	public static final String P_INSPECTOR_KILL_TIME_SECONDS = "inspectorKillTimePreference";
	
	/**
	 * Capabilities to hide in the selection inspector.
	 */
	public static final String P_INSPECTOR_HIDE = "inspectorHidePreference";
	
	/**
	 * Active type views.
	 */
	public static final String P_TYPE_VIEWS = "typeViewsPreference";

	/**
	 * Global capability options.
	 */
	public static final String P_CAPABILITY_OPTIONS = "capabilityOptionPreference";
	
}
