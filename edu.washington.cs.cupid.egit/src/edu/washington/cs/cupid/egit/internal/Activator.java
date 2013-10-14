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
package edu.washington.cs.cupid.egit.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.egit.GitHistoryCapability;
import edu.washington.cs.cupid.egit.GitWasModifiedCapability;
import edu.washington.cs.cupid.egit.GitModifiedFilter;
import edu.washington.cs.cupid.egit.GitProjectRepositoriesCapability;
import edu.washington.cs.cupid.egit.LastProjectRevision;

/**
 * The activator for the Cupid EGit capabilities plug-in.
 */
public final class Activator extends AbstractUIPlugin implements ICapabilityPublisher{

	/**
	 * The id for the Cupid EGit capabilities plug-in.
	 */
	public static final String PLUGIN_ID = "edu.washington.cupid.egit"; //$NON-NLS-1$

	private static Activator plugin;
	
	/**
	 * Construct the activator for the Cupid EGit capabilities plug-in.
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
	 * Returns the shared instance.
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void addChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

	@Override
	public void removeChangeListener(ICapabilityChangeListener listener) {
		// NO OP
	}

	@Override
	public ICapability[] publish() {
		return new ICapability[] {
			new GitHistoryCapability(),
			new GitProjectRepositoriesCapability(),
			new GitModifiedFilter(),
			new GitWasModifiedCapability(),
			new LastProjectRevision(),
		};
	}

}
