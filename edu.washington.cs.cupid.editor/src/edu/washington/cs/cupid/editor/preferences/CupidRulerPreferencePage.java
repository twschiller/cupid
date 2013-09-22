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
package edu.washington.cs.cupid.editor.preferences;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import edu.washington.cs.cupid.editor.Activator;
import edu.washington.cs.cupid.editor.LineProvider;
import edu.washington.cs.cupid.editor.RulerUtil;

/**
 * Cupid selection inspector preference page.
 * @author Todd Schiller
 */
public final class CupidRulerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private List<RulerPreference> model = Lists.newArrayList();
	private List<LineProvider> providers;
	private TableViewer viewer;
	
	private IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
	
	/**
	 * Construct the Cupid selection inspector preference page.
	 */
	public CupidRulerPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Cupid Editor Ruler Preference Page");
	}
	
	@Override
	public void init(final IWorkbench workbench) {
		// NO OP
	}
		
	@Override
	protected Control createContents(final Composite parent) {
		List<RulerPreference> current = Activator.getDefault().getRulerPreferences();
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		final int margin = 5;
		layout.numColumns = 1;
		layout.marginRight = margin;
		layout.marginTop = margin;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Active Capabilities:");
		
		viewer = new TableViewer(composite);
		final Table table = viewer.getTable();
		viewer.setContentProvider(new ArrayContentProvider());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		   
		data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		
		table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				// height cannot be per row so simply set
				event.height = 20;
			}
		});
		
		TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("Capability");
        column.setWidth(100);
        TableViewerColumn capabilityColumn = new TableViewerColumn(viewer, column);
        capabilityColumn.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object elt) {
                return ((RulerPreference)elt).capability;
            }
        });
		     
        column = new TableColumn(table, SWT.NONE);
        column.setText("Output");
        column.setWidth(100);
        TableViewerColumn outputColumn = new TableViewerColumn(viewer, column);
        outputColumn.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object elt) {
                return ((RulerPreference)elt).output;
            }
        });
		
        column = new TableColumn(table, SWT.NONE);
        column.setText("Color");
        column.setWidth(100);
        TableViewerColumn actionsNameCol = new TableViewerColumn(viewer, column);
        
        actionsNameCol.setLabelProvider(new OwnerDrawLabelProvider() {
			@Override
			protected void measure(Event event, Object element) {
				 event.setBounds(new Rectangle(event.x, event.y, 100, event.height));  
			}

			@Override
			protected void paint(Event event, Object element) {
				TableItem item = (TableItem) event.item;
				RulerPreference pref = (RulerPreference) item.getData();
				
				if (pref.color != null){
					event.gc.setBackground(new Color(CupidRulerPreferencePage.this.getShell().getDisplay(), pref.color));		
					event.gc.fillRectangle(new Rectangle(event.x, event.y, 200, event.height));
				}
			}
        });
        actionsNameCol.setEditingSupport(new ColorEditingSupport(viewer));
           
        column = new TableColumn(table, SWT.NONE);
        column.setText("Enabled?");
        column.setWidth(60);
        TableViewerColumn enabledColumn = new TableViewerColumn(viewer, column);
        enabledColumn.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object elt) {
                return ((RulerPreference)elt).enabled ? "Enabled" : "Disabled";
            }
        });
        enabledColumn.setEditingSupport(new EnabledEditingSupport(viewer));
		
        
		providers = RulerUtil.allLineProviders(current, false);
		for (LineProvider provider : providers) {
			RulerPreference pref = RulerUtil.getPreference(provider, current);
			
			if (pref != null){
				model.add(pref);
			}else{
				model.add(new RulerPreference(
						provider.getCapability().getName(), provider.getOutput().getName(),
						provider.getColor(), true));
			}
		}

		viewer.setInput(model);
		viewer.getTable().layout(true);
		return composite;
	}
	
	public class EnabledEditingSupport extends EditingSupport{
		private final TableViewer viewer;

		public EnabledEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}
		
		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((RulerPreference) element).enabled;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((RulerPreference) element).enabled = (Boolean) value;
			viewer.update(element, null);
		}
	}
	
	public class ColorEditingSupport extends EditingSupport {
		private final TableViewer viewer;

		public ColorEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new ColorCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((RulerPreference) element).color;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((RulerPreference) element).color = (RGB) value;
			viewer.update(element, null);
		}
	} 
	
	@Override
	protected void performApply() {
		preferences.setValue(PreferenceConstants.P_RULER_PREFERENCES, new Gson().toJson(model));
		super.performApply();
	}
}

