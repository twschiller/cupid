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

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.framework.Bundle;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import edu.washington.cs.cupid.usage.internal.Activator;

public class UsagePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

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
			session.setText(Activator.getDefault().getCollector().getAllJson("   "));
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
		
		enabled = new Button(composite, SWT.CHECK);
		enabled.setText("Enable Cupid Data Reporting");
		enabled.setSelection(preferences.getBoolean(PreferenceConstants.P_ENABLE_COLLECTION));
		
		Button delete = new Button(composite, SWT.PUSH);
		delete.setText("Erase Workspace Usage Data");
		
		tabs = new TabFolder(composite, SWT.BOTTOM);
		consentTab = new TabItem(tabs, SWT.NONE);
		consentTab.setText("Consent Agreement");
			
		Browser consentText = new Browser(tabs, SWT.BORDER);
		
		try{
			Bundle bundle = Activator.getDefault().getBundle();
			URL fileURL = bundle.getEntry("documents/consent-agreement.html");
			File file = new File(FileLocator.resolve(fileURL).toURI());
		
			String content = Joiner.on(System.getProperty("line.separator")).join(Files.readLines(file, Charset.defaultCharset()));
			consentText.setText(content);
		} catch (Exception ex) {
			consentText.setText("Error loading consent form: " + ex.getLocalizedMessage());
		}
		
		consentTab.setControl(consentText);
		
		dataTab = new TabItem(tabs, SWT.NONE);
		dataTab.setText("Workspace Data Preview");
		
		session = new Text(tabs, SWT.MULTI | SWT.BORDER);
		session.setEditable(false);		
		updatePreview();
		dataTab.setControl(session);
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		data.heightHint = 350;
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
