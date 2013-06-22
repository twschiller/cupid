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
package edu.washington.cs.cupid.wizards.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;

/**
 * The activator class for the Cupid wizards plug-in.
 */
public final class Activator extends AbstractUIPlugin implements ICapabilityPublisher {

	private static final String CAPABILITY_EXTENSION = ".arrow";

	/**
	 *  The plug-in ID for the Cupid wizards plug-in.
	 */
	public static final String PLUGIN_ID = "edu.cs.washington.cs.cupid.wizards"; //$NON-NLS-1$

	private static Activator plugin;
	
	private static ILog pluginLog;
	
	private HydrationService hydrate = new HydrationService();
	
	/**
	 * Construct the activator class for the Cupid wizards plug-in.
	 */
	public Activator() {
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pluginLog = Platform.getLog(context.getBundle());
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	public HydrationService getHydrationService(){
		return hydrate;
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public void addChangeListener(final ICapabilityChangeListener listener) {
		// NO OP
	}

	@Override
	public void removeChangeListener(final ICapabilityChangeListener listener) {
		// NO OP
	}
	
	@Override
	public ICapability[] publish() {
		
		File dir = CupidPlatform.getPipelineDirectory();
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(final File dirName, final String name) {
				return name.endsWith(CAPABILITY_EXTENSION);
			}
		};
		
		List<ICapability> result = Lists.newArrayList();
		
		if (dir.isDirectory()) {
			for (File file : dir.listFiles(filter)) {
				try {
					result.add(hydrate.hydrate(file));
				} catch (Exception e) {
					logError("Error hydrating capability " + file.getAbsolutePath(), e);
				}
			}
		} else {
			logError(dir.getAbsolutePath() + " is not a valid directory", new IOException(dir.getAbsolutePath() + " is not a valid directory"));
		}
		
		return result.toArray(new ICapability[]{});
	}

	/**
	 * Log an error in the plugin's log.
	 * @param msg localized error message
	 * @param e the exception
	 */
	public void logError(final String msg, final Exception e) {
		pluginLog.log(new Status(Status.ERROR, PLUGIN_ID, Status.ERROR, msg, e));			
	}
	
}
