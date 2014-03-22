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
package edu.washington.cs.cupid.junit.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
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
import edu.washington.cs.cupid.capability.linear.LinearPipeline;
import edu.washington.cs.cupid.junit.JUnitMonitor;
import edu.washington.cs.cupid.junit.preferences.PreferenceConstants;
import edu.washington.cs.cupid.markers.IMarkerBuilder;
import edu.washington.cs.cupid.standard.Count;

/**
 * The activator for the Cupid JUnit plugin.
 */
public final class Activator extends AbstractUIPlugin implements ICapabilityPublisher {

	/**
	 * The Cupid Junit plug-in ID.
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.junit"; //$NON-NLS-1$

	private static Activator plugin;

	private final JUnitMonitor monitor = new JUnitMonitor();
	
	private static final ChangeNotifier CHANGE_NOTIFIER = new ChangeNotifier();
	
	private static final HashMap<String, Set<ICapability>> LAUNCH_CONFIGURATIONS = Maps.newHashMap();
	
	/**
	 * Construct the Cupid JUnit plugin.
	 */
	public Activator() {
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		monitor.start();
		
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (event.getProperty().equals(PreferenceConstants.P_ACTIVE)) {
					
					Set<String> current = readActiveConfigs();
					
					Set<ICapability> activated = Sets.newHashSet();
					for (String config : current){
						if (!LAUNCH_CONFIGURATIONS.containsKey(config)){
							addCapabilitiesForConfiguration(config);
							activated.addAll(LAUNCH_CONFIGURATIONS.get(config));
						}
					}
					for (ICapability c : activated){
						CHANGE_NOTIFIER.onCapabilityAdded(c);
					}
					
					Set<String> configs = LAUNCH_CONFIGURATIONS.keySet();
					for (String config : configs){
						if (!current.contains(config)){
							for (ICapability c : LAUNCH_CONFIGURATIONS.get(config)){
								CHANGE_NOTIFIER.onCapabilityRemoved(c);
							}
							LAUNCH_CONFIGURATIONS.remove(config);
						}
					}
				}
			}
		});
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
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
	public static JUnitMonitor getDefaultMonitor() {
		return plugin.monitor;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@SuppressWarnings("rawtypes")
	private void addCapabilitiesForConfiguration(String config){
		Set<ICapability> cs = Sets.newHashSet();
		
		cs.add(new LinearPipeline<IJavaProject, Integer>(
				"Test Failure Count (" + config + ")", "The number of JUnit test failures (" + config + ")",
				new JUnitCapability(config), new JUnitFailures(), new Count()));
		
		cs.add(new LinearPipeline<IJavaProject, Collection<IMarkerBuilder>>(
				"JUnit Test Failure Markers (" + config + ")", "JUnit test failure markers (" + config + ")",
				new JUnitCapability(config), new JUnitMarkers()));
		
		LAUNCH_CONFIGURATIONS.put(config, cs);
	}
	
	private Set<String> readActiveConfigs(){
		String val = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_ACTIVE);
		return "".equals(val) ? Sets.<String>newHashSet() : Sets.newHashSet(val.split(";"));
	}
	
	@Override
	public ICapability[] publish() {
		List<ICapability> capabilities = Lists.newArrayList();
		
		for (String config : readActiveConfigs()) {
			if (!config.isEmpty()) {
				if (!LAUNCH_CONFIGURATIONS.containsKey(config)) {
					addCapabilitiesForConfiguration(config);
				}
				capabilities.addAll(LAUNCH_CONFIGURATIONS.get(config));
			}
		}
		
		return capabilities.toArray(new ICapability[]{});
	}

	@Override
	public void addChangeListener(final ICapabilityChangeListener listener) {
		CHANGE_NOTIFIER.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(final ICapabilityChangeListener listener) {
		CHANGE_NOTIFIER.removeChangeListener(listener);
	}
}
