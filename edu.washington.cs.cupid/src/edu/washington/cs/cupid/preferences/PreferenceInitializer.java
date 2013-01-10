package edu.washington.cs.cupid.preferences;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;

import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.views.ViewRule;


public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = CupidActivator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_IMPURE, false);
		store.setDefault(PreferenceConstants.P_JOB_STATUS_LOGGING, true);
		store.setDefault(PreferenceConstants.P_CACHE_STATUS_LOGGING, true);
		store.setDefault(PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS, 10);
		store.setDefault(PreferenceConstants.P_ARROW_DIR, new File(System.getProperty("user.home"),".cupid").getAbsolutePath());
	
		store.setDefault(PreferenceConstants.P_TYPE_VIEWS, new Gson().toJson(new ArrayList<ViewRule>()));
	
	}
}
