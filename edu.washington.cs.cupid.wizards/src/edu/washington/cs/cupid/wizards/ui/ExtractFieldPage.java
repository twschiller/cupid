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
package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.cupid.wizards.internal.Getter;

public class ExtractFieldPage extends WizardPage {

	private Class<?> startClazz;
	private SelectAccessorWidget selector;
	
	protected ExtractFieldPage(Class<?> clazz) {
		super("Extract Field");
		this.startClazz = clazz;
	}
	
	@Override
	public void createControl(Composite parent) {
		this.setTitle("Extract information");
		this.setMessage("Select a type then select a getter or field");
		
		Composite container = new Composite(parent, SWT.NONE);
	    GridLayout layout = new GridLayout();
	    container.setLayout(layout);
	    container.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.selector = new SelectAccessorWidget(container, startClazz, SWT.NONE);
		this.selector.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.setControl(selector);
		
		this.selector.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				getWizard().getContainer().updateButtons();
			}
		});
	}
	
	public boolean hasSelection(){
		return selector.hasSelection();
	}
	
	public Getter<?, ?> getGetter() throws Exception{
		return selector.getGetter();
	}
}
