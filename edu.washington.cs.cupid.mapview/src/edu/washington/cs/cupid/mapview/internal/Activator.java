package edu.washington.cs.cupid.mapview.internal;

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
	
	/**
	 * The constructor.
	 */
	public Activator() {
	}

	@Override
	public final void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
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

}
