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

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.framework.BundleContext;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.exception.MalformedCapabilityException;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.conditional.Formatter;
import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.FormattingRuleManager;

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

	private static ILog pluginLog;
	
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
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		
		ruleManager = FormattingRuleManager.getInstance();
		formatter = new Formatter(workbench, ruleManager);
		
		getPreferenceStore().addPropertyChangeListener(ruleManager);
		
		pluginLog = Platform.getLog(context.getBundle());
	
		new FormatWorkbenchJob().schedule();
	}
	
	public class FormatWorkbenchJob extends WorkbenchJob{
		public FormatWorkbenchJob() {
			super("Format Workbench");
		
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try{
				formatter.formatActiveWindow();
				return Status.OK_STATUS;
			}finally{
				monitor.done();
			}
		}
	}
	
	
	@Override
	public void earlyStartup() {
		// NOP	
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
	 * @throws MalformedCapabilityException iff the corresponding available capability has a mismatched type
	 * @throws ClassNotFoundException iff the input type is not found
	 */
	public static ICapability findRuleCapability(final FormattingRule rule) throws NoSuchCapabilityException, MalformedCapabilityException, ClassNotFoundException {
		Preconditions.checkNotNull(rule.getCapabilityId(), "Formatting rule must be associated with a capability");
		
		TypeToken<?> inputType = TypeManager.forName(rule.getQualifiedType());
		ICapability capability = CupidPlatform.getCapabilityRegistry().findCapability(rule.getCapabilityId());

		if (CapabilityUtil.isLinear(capability) 
				&& !CapabilityUtil.isGenerator(capability)
				&& TypeManager.isJavaCompatible(CapabilityUtil.unaryParameter(capability).getType(), inputType)){
			
			return capability;
		} else {
			throw new MalformedCapabilityException(capability, "Formatting rule requires linear predicate capability");
		}
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
