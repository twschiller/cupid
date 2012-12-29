package edu.washington.cs.cupid.pipes;

import java.util.List;
import java.util.Set;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.capability.ChangeNotifier;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements ICapabilityPublisher, ICapabilityChangeListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.pipes"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private final ChangeNotifier notifier = new ChangeNotifier();
	
	private final List<ICapability<?,?>> pipes = Lists.newArrayList();
	
	private Set<ICapability<?,?>> available = Sets.newIdentityHashSet();
	
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
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static Set<ICapability<?,?>> getCapabilities(){
		return getDefault().available;
	}

	public void registerPipeline(ICapability<?,?> capability){
		pipes.add(capability);
		notifier.onChange(this);
	}
	
	@Override
	public void addChangeListener(ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}

	@Override
	public ICapability<?, ?>[] publish() {
		return pipes.toArray(new ICapability[]{});
	}

	@Override
	public void onChange(ICapabilityPublisher publisher) {
		available = Sets.newHashSet(publisher.publish());
	}

}
