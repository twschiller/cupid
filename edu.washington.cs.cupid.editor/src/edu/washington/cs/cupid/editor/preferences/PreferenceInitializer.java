package edu.washington.cs.cupid.editor.preferences;

import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;

import edu.washington.cs.cupid.editor.Activator;


/**
 * The Cupid editor preference initializer.
 * @author Todd Schiller 
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_RULER_PREFERENCES, new Gson().toJson(new ArrayList<RulerPreference>()));	
	}
}

