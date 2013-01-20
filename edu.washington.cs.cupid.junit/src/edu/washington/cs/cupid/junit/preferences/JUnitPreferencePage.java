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
package edu.washington.cs.cupid.junit.preferences;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.junit.internal.Activator;

public class JUnitPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Table table;
	
	public JUnitPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Cupid JUnit Preference Page");
	}
	
	@Override
	public void init(IWorkbench workbench) {
	}
	
	public void update(){
		List<String> active = Lists.newArrayList();
		
		for (TableItem item : table.getItems()){
			if (item.getChecked()){
				active.add(item.getText());
			}
		}
		
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_ACTIVE, Joiner.on(";").join(active));
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Set<String> current = Sets.newHashSet(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_ACTIVE).split(";"));
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginRight = 5;
		layout.marginTop = 5;
		layout.marginWidth  = 0;
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Active JUnit Capabilities:");
		
		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		
		String[] titles = { "Test Configuration" };
		
		for (int i = 0 ; i < titles.length; i++){
			TableColumn column = new TableColumn(table, SWT.NULL);
			column.setText(titles[i]);
		}
		
		ILaunchManager launches = DebugPlugin.getDefault().getLaunchManager();
		try {
			for (ILaunchConfiguration config : launches.getLaunchConfigurations()){
				TableItem item = new TableItem(table, SWT.NULL);
				item.setText(config.getName());
				item.setText(0, config.getName());
				
				if (current.contains(config.getName())){
					item.setChecked(true);
				}
			}
		} catch (CoreException e) {
			// NO OP
		}
		
		for (int i = 0; i < titles.length; i++){
			table.getColumn(i).pack();
		}
		
		table.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK){
					update();
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
