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
package edu.washington.cs.cupid.conditional.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.gson.Gson;

import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.FormattingRuleManager;
import edu.washington.cs.cupid.conditional.internal.Activator;
import edu.washington.cs.cupid.conditional.internal.FormatUtil;

/**
 * Preference page for defining and editing conditional formatting rules.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class FormattingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	// TODO implement restore defaults
	
	private Composite composite;
	private Table table;
	private ToolBar toolbar;
			    
    /**
     * Construct the preference page for defining and editing conditional formatting rules.
     */
	public FormattingPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Conditional Formatting Rules");
	}
	
	@Override
	public void init(final IWorkbench workbench) {
		// NO OP
	}
	
	@Override
	public boolean performOk() {
		save();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		disableAll();
	}
	
	@Override
	protected Control createContents(final Composite parent) {	
		composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginRight = 5;
		layout.marginTop = 5;
		layout.marginWidth  = 0;
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		toolbar = new ToolBar(composite, SWT.HORIZONTAL | SWT.FLAT);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		toolbar.setLayoutData(data);
		
		final ToolItem add = new ToolItem(toolbar, SWT.PUSH);
		add.setText("Add");
		add.setToolTipText("Add Rule");
		add.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				FormattingRule rule = new FormattingRule("New Rule");
				addRuleItem(rule);
				table.select(table.getItemCount() - 1);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem edit = new ToolItem(toolbar, SWT.PUSH);
		edit.setText("Edit");
		edit.setToolTipText("Edit Rule");
		edit.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem item = table.getSelection()[0];
				FormattingRule rule = ((FormattingRule) item.getData());
				
				FormatRuleDialog dEdit = new FormatRuleDialog(rule, FormattingPreferencePage.this.getShell());
				dEdit.setBlockOnOpen(true);
				if (dEdit.open() == Dialog.OK){
					FormattingRule modified = dEdit.getRule();
					item.setData(modified);
					item.setText(modified.getName());
					FormatUtil.setFormat(null, item, modified.getFormat());	
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem copy = new ToolItem(toolbar, SWT.PUSH);
		copy.setText("Copy");
		copy.setToolTipText("Copy Rule");
		copy.setEnabled(false);
		copy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				TableItem item = table.getSelection()[0];
				
				FormattingRule clone = ((FormattingRule) item.getData()).copy();
				clone.setName(clone.getName() + " (Copy) ");
				addRuleItem(clone);
				table.select(table.getItemCount() - 1);
				copy.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem delete = new ToolItem(toolbar, SWT.PUSH);
		delete.setText("Delete");
		delete.setToolTipText("Delete Rule");
		delete.setEnabled(false);
		delete.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// TODO handle multiple selections
				table.remove(table.getSelectionIndex());
				delete.setEnabled(false);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem enable = new ToolItem(toolbar, SWT.PUSH);
		enable.setText("Enable");
		enable.setToolTipText("Enable All");
		enable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// TODO handle multiple selections
				for (TableItem item : table.getItems()) {
					item.setChecked(true);
					((FormattingRule) item.getData()).setActive(true);
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem disable = new ToolItem(toolbar, SWT.PUSH);
		disable.setText("Disable");
		disable.setToolTipText("Disable All");
		disable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				disableAll();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		table.setLayoutData(data);
		table.setHeaderVisible(false);
		
		String[] titles = { "Formatting Rule" };
		
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NULL);
			column.setText(titles[i]);
		}
		
		FormattingRule[] rules = new FormattingRule[]{};
		try {
			rules = FormattingRuleManager.getInstance().storedRules();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
				
		for (FormattingRule rule : rules) {
			addRuleItem(rule);
		}
	
		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}
		
		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				TableItem item = (TableItem) e.item;
				FormattingRule rule = ((FormattingRule) item.getData());
				rule.setActive(item.getChecked());		
				delete.setEnabled(true);
				copy.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		if (table.getItemCount() > 0) {
			table.select(0);
		}
				
		return null;
	}
	
	private void disableAll() {
		// TODO handle multiple selections
		for (TableItem item : table.getItems()) {
			item.setChecked(false);
			((FormattingRule) item.getData()).setActive(false);
		}
	}
	
	/**
	 * Add <code>rule</code> to the table, using the formatting described by <code>rule</code>.
	 * @param rule the rule
	 * @return the added item
	 */
	private TableItem addRuleItem(final FormattingRule rule) {
		TableItem item = new TableItem(table, SWT.NULL);
		item.setText(rule.getName());
		item.setText(0, rule.getName());
		item.setChecked(rule.isActive());
		item.setData(rule);
		
		FormatUtil.setFormat(null, item, rule.getFormat());	
		
		return item;
	}
	
	/**
	 * Save the formatting rules to the preference store in JSON format.
	 */
	private void save() {
		Gson gson = new Gson();
		FormattingRule[] rules = new FormattingRule[table.getItemCount()];
		for (int i = 0; i < rules.length; i++) {
			rules[i] = (FormattingRule) table.getItem(i).getData();
		}
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_RULES, gson.toJson(rules));
	}
}
