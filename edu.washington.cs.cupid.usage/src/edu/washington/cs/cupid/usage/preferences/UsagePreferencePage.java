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
package edu.washington.cs.cupid.usage.preferences;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.internal.image.GIFFileFormat;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.google.common.io.CharStreams;

import edu.washington.cs.cupid.usage.internal.Activator;
import edu.washington.cs.cupid.usage.internal.SurveyDialog;

public class UsagePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String CONSENT_AGREEMENT_PATH = "/documents/consent-agreement.html"; //$NON-NLS-1$
	
	private TabFolder tabs;
	private TabItem consentTab;
	private TabItem dataTab;
	private Button enabled;
	private Text session;
	
	final IPreferenceStore preferences;
	
	public UsagePreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Cupid Usage Data Reporting Preferences");
		preferences = Activator.getDefault().getPreferenceStore();
	}
	
	@Override
	public void init(IWorkbench workbench) {
		// NO OP
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		enabled.setSelection(preferences.getDefaultBoolean(PreferenceConstants.P_ENABLE_COLLECTION));
		updatePreview();
	}
	
	@Override
	protected void performApply() {
		super.performApply();
		preferences.setValue(PreferenceConstants.P_ENABLE_COLLECTION, enabled.getSelection());
		updatePreview();
	}

	@Override
	public boolean performOk() {
		preferences.setValue(PreferenceConstants.P_ENABLE_COLLECTION, enabled.getSelection());
		return super.performOk();
	}
	
	private void updatePreview(){
		try{
			session.setText(Activator.getDefault().getCollector().getAllJson("   ", true));
		}catch(Exception ex){
			session.setText("Error reading workspace usage data: " + ex.getLocalizedMessage());
		}
	}
	
	@Override
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Link surveyLink = new Link(composite, SWT.LEFT);
		surveyLink.setText("Help improve Cupid by completing a short survey: <a href=\"" + SurveyDialog.DEV_SURVEY_URL + "\">Open in Browser.</a>");
		
		GridData dSurvey = new GridData(SWT.FILL, SWT.NONE, true, false);
		dSurvey.horizontalSpan = 2;
		surveyLink.setLayoutData(dSurvey);
		
		enabled = new Button(composite, SWT.CHECK);
		enabled.setText("Enable Cupid Data Reporting");
		enabled.setSelection(preferences.getBoolean(PreferenceConstants.P_ENABLE_COLLECTION));
		
		Button delete = new Button(composite, SWT.PUSH);
		delete.setText("Erase Workspace Usage Data");
		
		
		surveyLink.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					//  Open default external browser 
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(e.text));
				} catch (Exception ex) {
					Activator.getDefault().logError("Error loading Cupid survey in browser", ex);
				} 
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				//NOP
			}
		});
	
		tabs = new TabFolder(composite, SWT.BOTTOM);
		consentTab = new TabItem(tabs, SWT.NONE);
		consentTab.setText("Consent Agreement");
			
		Browser consentText = new Browser(tabs, SWT.BORDER);
		
		try{
			Bundle bundle = Activator.getDefault().getBundle();
			URL fileURL = bundle.getEntry(CONSENT_AGREEMENT_PATH);
			
			if (fileURL == null){
				throw new RuntimeException("Unable to locate consent agreement at " + CONSENT_AGREEMENT_PATH);
			}
			
			InputStream inputStream = fileURL.openStream();
			String content = CharStreams.toString(new InputStreamReader(inputStream, Charset.forName("UTF-8")) );
			
			consentText.setText(content);
		} catch (Exception ex) {
			consentText.setText("Error loading consent form: " + ex.getLocalizedMessage());
		}
		
		consentTab.setControl(consentText);
		
		dataTab = new TabItem(tabs, SWT.NONE);
		dataTab.setText("Workspace Data Preview");
		
		session = new Text(tabs, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		session.setEditable(false);		
		updatePreview();
		dataTab.setControl(session);
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		data.heightHint = 350;
		data.widthHint = 400;
		tabs.setLayoutData(data);
		
		delete.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean confirm = MessageDialog.openConfirm(
						UsagePreferencePage.this.getShell(), 
						"Delete Workspace Cupid Usage Data?",
						"Are you sure you want to delete the Cupid usage data stored for this workspace?");
				
				if (confirm){
					Activator.getDefault().getCollector().deleteLocalData();
					updatePreview();
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		return null;
	}	
}
