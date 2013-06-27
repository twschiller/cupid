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
package edu.washington.cs.cupid.conditional.internal;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.exception.MalformedCapabilityException;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.conditional.Formatter;
import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.FormattingRuleManager;
import edu.washington.cs.cupid.conditional.preferences.PreferenceConstants;

/**
 * Activator for the conditional formatting plug-in.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class Activator extends AbstractUIPlugin implements IStartup {

	/** 
	 * The plug-in ID for the conditional formatting plug-in.
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.conditional"; //$NON-NLS-1$

	private static Activator plugin;

	/**
	 * Construct the conditional formatting plug-in.
	 */
	public Activator() {
		// NO OP
	}

	/**
	 * Applies conditional formatting rules to views when they are activated.
	 */
	private Formatter formatter;
	
	private FormattingRuleManager ruleManager;

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		formatter = new Formatter();
		ruleManager = FormattingRuleManager.getInstance();
		
		getPreferenceStore().addPropertyChangeListener(ruleManager);
		
		final IWorkbench workbench = PlatformUI.getWorkbench();
		
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				
				for (IWorkbenchPage page : window.getPages()) {
					for (IViewReference view : page.getViewReferences()) {
						formatter.applyFormattingRules(view);
					}
					
					page.addPartListener(formatter);
				}
				
				window.addPageListener(new IPageListener() {
					@Override
					public void pageActivated(final IWorkbenchPage page) {
						page.addPartListener(formatter);
					}
					@Override
					public void pageClosed(final IWorkbenchPage page) {
						page.removePartListener(formatter);
					}
					@Override
					public void pageOpened(final IWorkbenchPage page) {
						page.addPartListener(formatter);
					}
				});
			}
		});
	}
	
	@Override
	public void earlyStartup() {
		// NO OP?	
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	
	
	/**
	 * Returns the predicate capability for <code>rule</code>.
	 * @param rule the formatting rule.
	 * @return the predicate capability for <code>rule</code>
	 * @throws NoSuchCapabilityException iff the rule has no associated capability id, or no capability is available with the given id
	 * @throws MalformedCapabilityException iff the corresponding available capability is not a predicate 
     * (i.e., does not have a boolean return value)
	 */
	public static ICapability findPredicate(final FormattingRule rule) throws NoSuchCapabilityException, MalformedCapabilityException {
		Preconditions.checkNotNull(rule.getCapabilityId(), "Formatting rule must be associated with a capability");
		
		ICapability capability = CupidPlatform.getCapabilityRegistry().findCapability(rule.getCapabilityId());

		if (CapabilityUtil.isLinear(capability) 
			&& TypeManager.isJavaCompatible(TypeToken.of(Boolean.class), CapabilityUtil.singleOutput(capability).getType())){
			
			return capability;
		} else {
			throw new MalformedCapabilityException(capability, "Formatting rule requires linear predicate capability");
		}
	}

}
