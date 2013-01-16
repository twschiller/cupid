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

}
