package edu.washington.cs.cupid.editor;

import java.util.List;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.washington.cs.cupid.editor.preferences.PreferenceConstants;
import edu.washington.cs.cupid.editor.preferences.RulerPreference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.editor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
		
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public List<RulerPreference> getRulerPreferences(){
		Gson gson = new Gson();
		String current = getPreferenceStore().getString(PreferenceConstants.P_RULER_PREFERENCES);
		List<RulerPreference> rules =
			gson.fromJson(current, new TypeToken<List<RulerPreference>>(){}.getType());
		return rules;
	}
	
}
