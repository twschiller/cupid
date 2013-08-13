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
package edu.washington.cs.cupid.usage.preferences;

/**
 * Constant definitions for plug-in preferences.
 */
public final class PreferenceConstants {

	private PreferenceConstants() {
		// NO OP
	}
	
	/**
	 * <code>true</code> indicates that usage data should be reported.
	 */
	public static final String P_ENABLE_COLLECTION = "enablePreference";

	/**
	 * How often usage data should be reported to the server.
	 */
	public static final String P_REPORT_FRQUENCY = "frequencyPreference";
	
	/**
	 * <code>true</code> iff the user has been shown the enable reporting dialog. 
	 */
	public static final String P_SHOWN_ENABLE_DIALOG = "showEnablePreference";
	
	/**
	 * <code>true</code> iff the user has been shown the enable reporting dialog. 
	 */
	public static final String P_UUID = "uuidPreference";
	
	/**
	 * The date of the next survey
	 */
	public static final String P_NEXT_SURVEY_DATE = "nextSurveyDate";
	
	/**
	 * <code>true</code> iff user should be notified to take the survey
	 */
	public static final String P_REMIND_SURVEY = "doRemindSurvey";
	
	
}
