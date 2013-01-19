package edu.washington.cs.cupid.conditional.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.washington.cs.cupid.conditional.internal.Activator;

/**
 * Class used to initialize default preference values.
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_RULES, "");
	}

}
