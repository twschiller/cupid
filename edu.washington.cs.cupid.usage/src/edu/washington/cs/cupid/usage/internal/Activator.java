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
package edu.washington.cs.cupid.usage.internal;

import java.util.Date;
import java.util.UUID;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.preferences.PreferenceConstants;

/**
 * Controls the plug-in life cycle for the usage data collector plug-in.
 */
public final class Activator extends AbstractUIPlugin implements IStartup, IPropertyChangeListener{

	/**
	 *  The plug-in ID for the usage data collector plug-in.
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.usage"; //$NON-NLS-1$

	private static Activator plugin;
	
	private ILog pluginLog;
	
	private CupidDataCollector collector;
	
	private static final boolean USAGE_ENABLED = false;
	
	/**
	 * The constructor
	 */
	public Activator() {
		
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		pluginLog = Platform.getLog(context.getBundle());
		collector = CupidDataCollector.getInstance();
		
		final IPreferenceStore preferences = getPreferenceStore();
		
		if (preferences.getString(PreferenceConstants.P_UUID).equals("")){
			preferences.setValue(PreferenceConstants.P_UUID, UUID.randomUUID().toString());
		}
		
		preferences.addPropertyChangeListener(this);
		
		if (USAGE_ENABLED && preferences.getBoolean(PreferenceConstants.P_ENABLE_COLLECTION)){
			collector.start();
			collector.upload.schedule(1000 * 10 /* 10 s */);
		}
		
		if (USAGE_ENABLED && !preferences.getBoolean(PreferenceConstants.P_SHOWN_ENABLE_DIALOG)){
			new UIJob("Cupid Data Collection Dialog"){

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					IShellProvider workbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					
					if (workbench != null) {
						Shell shell = workbench.getShell();
						
						if (shell != null) {
							DataCollectorDialog dialog = new DataCollectorDialog(shell);
							dialog.create();
							dialog.setBlockOnOpen(true);
							
							if (dialog.open() == Dialog.OK) {
								preferences.setValue(PreferenceConstants.P_ENABLE_COLLECTION, true);
							} else {
								preferences.setValue(PreferenceConstants.P_ENABLE_COLLECTION, false);	
							}
						
							preferences.setValue(PreferenceConstants.P_SHOWN_ENABLE_DIALOG, true);
						}
					}
	
					return Status.OK_STATUS;
				}
			}.schedule();
		}
		
		boolean doSurvey = preferences.getBoolean(PreferenceConstants.P_REMIND_SURVEY);
		Date next = new Date(preferences.getLong(PreferenceConstants.P_NEXT_SURVEY_DATE));
		Date today = new Date();
		
		if (USAGE_ENABLED && doSurvey && today.after(next)){
			new UIJob("Usage Survey Job"){
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					IShellProvider workbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					
					if (workbench != null){
						SurveyDialog dialog = new SurveyDialog(workbench.getShell());
						int result = dialog.open();
						
						if (result == IDialogConstants.ABORT_ID){
							preferences.setValue(PreferenceConstants.P_REMIND_SURVEY, false);	
						}else if (result == IDialogConstants.OK_ID){
							preferences.setValue(PreferenceConstants.P_REMIND_SURVEY, false);
						}else if (result == IDialogConstants.IGNORE_ID){
							preferences.setValue(PreferenceConstants.P_REMIND_SURVEY, true);
							preferences.setValue(
									PreferenceConstants.P_NEXT_SURVEY_DATE, 
									SurveyDialog.addDaysToDate(new Date(), 7).getTime());
						}else if (result == IDialogConstants.CANCEL_ID){
							// NOP
						}else{
							Activator.getDefault().logError("Unexpected result from Cupid survey dialog", null);
						}
					}
					
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin.collector.stop();
		plugin = null;
		super.stop(context);
	}

	
	public CupidDataCollector getCollector() {
		return collector;
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void earlyStartup() {
		// NO OP
	}
	
	/**
	 * Log an error in the plugin's log.
	 * @param msg localized error message
	 * @param e the exception
	 */
	public void logError(final String msg, final Exception e) {
		plugin.pluginLog.log(new Status(Status.ERROR, PLUGIN_ID, Status.ERROR, msg, e));			
	}

	/**
	 * Log information in the plugin's log.
	 * @param msg localized information message
	 */
	public void logInformation(final String msg) {
		plugin.pluginLog.log(new Status(Status.INFO, PLUGIN_ID, Status.INFO, msg, null));
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_ENABLE_COLLECTION)){
			if ((Boolean) event.getNewValue()){
				try {
					plugin.collector.start();
					logInformation("Started Cupid data collector");
				} catch (Exception ex){
					logError("Error starting Cupid data collector", ex);
				}
			} else {
				try {
					plugin.collector.stop();
					logInformation("Stopped Cupid data collector");
				} catch (Exception ex){
					logError("Error stopping Cupid data collector", ex);
				}
			}
		}
	}
	
}
