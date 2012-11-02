package edu.washington.cs.cupid.junit.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.capability.ChangeNotifier;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.capability.LinearPipeline;
import edu.washington.cs.cupid.junit.JUnitMonitor;
import edu.washington.cs.cupid.junit.preferences.PreferenceConstants;
import edu.washington.cs.cupid.standard.Count;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements ICapabilityPublisher {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.junit"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	private static Activator plugin;

	private final JUnitMonitor monitor = new JUnitMonitor();
	
	private static final ChangeNotifier notifier = new ChangeNotifier();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@SuppressWarnings("rawtypes")
	private static final HashMap<String, Set<ICapability>> configs = Maps.newHashMap();
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		monitor.start();
		
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(PreferenceConstants.P_ACTIVE)){
					notifier.onChange(Activator.this);
				}
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		monitor.stop();
	}

	/**
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * @return the shared monitor
	 */
	public static JUnitMonitor getDefaultMonitor(){
		return plugin.monitor;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ICapability<?,?>[] publish() {
		List<ICapability> capabilities = Lists.newArrayList();
		
		Set<String> current = Sets.newHashSet(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_ACTIVE).split(";"));
		
		for (String config : current){
			if (!config.isEmpty()){
				if (!configs.containsKey(config)){
					configs.put(config, Sets.<ICapability>newHashSet());
					
					configs.get(config).add(
							new LinearPipeline.PipelineBuilder(new JUnitCapability(config))
							.attach(new JUnitFailures())
							.attach(new Count())
							.create("Test Failure Count (" + config + ")", 
									"The number of JUnit test failures (" + config + ")"));
					
					configs.get(config).add(
							new LinearPipeline.PipelineBuilder(new JUnitCapability(config))
							.attach(new JUnitMarkers())
							.create("Place Test Failure Markers (" + config + ")", 
									"The number of JUnit test failures (" + config + ")"));
				}
				
				capabilities.addAll(configs.get(config));
			}
		}
		
		return capabilities.toArray(new ICapability[]{});
	}

	@Override
	public void addChangeListener(ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ICapabilityChangeListener listener) {
		notifier.removeChangeListener(listener);
	}
}
