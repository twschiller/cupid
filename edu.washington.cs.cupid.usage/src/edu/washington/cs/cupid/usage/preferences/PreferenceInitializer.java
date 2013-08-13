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

import java.util.Date;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.washington.cs.cupid.usage.internal.Activator;
import edu.washington.cs.cupid.usage.internal.SurveyDialog;

/**
 * Class used to initialize default preference values.
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer {
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_ENABLE_COLLECTION, false);
		store.setDefault(PreferenceConstants.P_SHOWN_ENABLE_DIALOG, false);
		store.setDefault(PreferenceConstants.P_REPORT_FRQUENCY, 1);
		store.setDefault(PreferenceConstants.P_NEXT_SURVEY_DATE, SurveyDialog.addDaysToDate(new Date(), 7).getTime());
		store.setDefault(PreferenceConstants.P_REMIND_SURVEY, true);
	}

}
