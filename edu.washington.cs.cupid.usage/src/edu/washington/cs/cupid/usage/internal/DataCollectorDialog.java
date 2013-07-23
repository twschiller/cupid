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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

/**
 * Dialog to prompt user to report usage data.
 * @author Todd Schiller
 */
public final class DataCollectorDialog extends TitleAreaDialog {
	
	/**
	 * Construct dialog to prompt user to report usage data.
	 * @param parentShell the parent SWT shell
	 */
	public DataCollectorDialog(final Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	public void create() {
		super.create();
		setTitle("Enable Cupid Usage Data Collection?");
		setMessage("Help improve Cupid by automatically reporting Cupid usage information", IMessageProvider.INFORMATION);
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		 GridData gridData = new GridData(SWT.CENTER, SWT.FILL, true, true);
		 parent.setLayoutData(gridData);
		 
		 createButton(parent, IDialogConstants.OK_ID, "Enable Data Collection", true);
		 createButton(parent, IDialogConstants.CANCEL_ID, "Do Not Collect Data", false);
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
		
		Label text = new Label(parent, SWT.LEFT | SWT.WRAP);
		text.setText("You can review collected data and disable reporting on the Cupid preferences page.");
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 30;
		text.setLayoutData(data);
		
		Browser consentText = new Browser(parent, SWT.BORDER);
		
		try {
			Bundle bundle = Activator.getDefault().getBundle();
			URL fileURL = bundle.getEntry("documents/consent-agreement.html");
			InputStream inputStream = fileURL.openConnection().getInputStream();
			String content = CharStreams.toString( new InputStreamReader( inputStream, Charset.forName("UTF-8")) );
			consentText.setText(content);
		} catch (Exception ex) {
			consentText.setText("Error loading consent form: " + ex.getLocalizedMessage());
		}
		
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 250;
		data.widthHint = 400;
		consentText.setLayoutData(data);
		
		return parent;
	}
	
}
