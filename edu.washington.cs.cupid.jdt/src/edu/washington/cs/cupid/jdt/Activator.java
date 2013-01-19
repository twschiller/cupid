package edu.washington.cs.cupid.jdt;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.jdt.compiler.CompilerMessagePredicate;
import edu.washington.cs.cupid.jdt.compiler.CompilerMessagesCapability;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements ICapabilityPublisher {

	/**
	 *  The plug-in ID
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.jdt"; //$NON-NLS-1$

	/**
	 *  The shared instance
	 */
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
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

	@Override
	public ICapability<?, ?>[] publish() {
		return new ICapability<?,?>[]{ 
				new CompilerMessagesCapability(), 
				new CompilerMessagePredicate()
		};
	}

	@Override
	public void addChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

	@Override
	public void removeChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

}
