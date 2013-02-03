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
package edu.washington.cs.cupid.preferences;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.options.IConfigurableCapability;
import edu.washington.cs.cupid.capability.options.Option;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.views.OptionEditorFactory;
import edu.washington.cs.cupid.views.OptionEditorFactory.OptionEditor;

/**
 * Preference page for setting capability options.
 * @author Todd Schiller
 */
public class CapabilityOptionsPage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private TableViewer capabilityTable;
	private Group optionsGroup;
	
	private IConfigurableCapability<?,?> selected;
	
	private List<Widget> optionWidgets = Lists.newArrayList();
	private BiMap<Option<?>, OptionEditor<?>> optionInputs = HashBiMap.create();

	/**
	 * Construct the preference page for creating type display rules.
	 */
	public CapabilityOptionsPage() {
		setPreferenceStore(CupidActivator.getDefault().getPreferenceStore());
		setDescription("Cupid Capability Options Page");
	}
	
	@Override
	protected void performApply() {
		for (Entry<Option<?>, OptionEditor<?>> entry : optionInputs.entrySet()){
			OptionEditor input = entry.getValue();
			
			if (input.getValue() != null && !input.getValue().equals(input.getOption().getDefault())){
				CupidActivator.getDefault().getCapabilityOptions().set(selected, input.getOption(), input.getValue());
			}
		}
	}
	

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		if (selected != null){
			showOptions(selected, true);
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		// NO OP	
	}

	@Override
	protected Control createContents(Composite parent) {
		final int margin = 5;
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginRight = margin;
		layout.marginTop = margin;
		layout.marginWidth  = 0;
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		makeCapabilityTable(composite);
		makeCapabilityOptions(composite);
			
		return null;
	}

	private TableViewerColumn createColumn(final String title, TableViewer table) {
	    final TableViewerColumn viewerColumn = new TableViewerColumn(table, SWT.LEFT);
	    final TableColumn column = viewerColumn.getColumn();
	    column.setText(title);
	    column.setResizable(true);
	    column.setMoveable(false);
	    return viewerColumn;
	}
	
	private void makeCapabilityTable(final Composite parent){
		capabilityTable = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL);
		
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
		data.heightHint = 200;
		capabilityTable.getTable().setLayoutData(data);
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(2));
		capabilityTable.getTable().setLayout(layout);
		
		final TableViewerColumn nameColumn = createColumn("Name", capabilityTable);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((ICapability<?, ?>) element).getName();
			}
		});
		
		final TableViewerColumn descriptionColumn = createColumn("Description", capabilityTable);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((ICapability<?, ?>) element).getDescription();
			}
		});
		
		capabilityTable.getTable().setHeaderVisible(true);
		capabilityTable.getTable().setLinesVisible(true);
		
		capabilityTable.setContentProvider(ArrayContentProvider.getInstance());
		capabilityTable.setInput(Lists.newArrayList(Iterables.filter(CupidPlatform.getCapabilityRegistry().getCapabilities(), IConfigurableCapability.class)));
	
		capabilityTable.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) capabilityTable.getSelection();
				selected = (IConfigurableCapability<?,?>)  selection.getFirstElement();
				showOptions(selected, false);
			}		
		});
	}
	
	private void makeCapabilityOptions(final Composite parent){
		optionsGroup = new Group(parent, SWT.BORDER);
		optionsGroup.setText("Options");
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		optionsGroup.setLayoutData(data);
	
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		optionsGroup.setLayout(layout);
	}
	
	private void clearOptions(){
		for (Widget control : optionWidgets){
			control.dispose();
		}
		
		optionWidgets.clear();
		optionInputs.clear();
		optionsGroup.layout(true);
	}
	
	private void showOptions(final IConfigurableCapability<?,?> capability, boolean showDefault){
		if (!optionWidgets.isEmpty()){
			clearOptions();
		}
		
		for (Option<?> option : capability.getOptions()){
			Label label = new Label(optionsGroup, SWT.LEFT);
			label.setText(option.getName());
			
			OptionEditor<?> input = OptionEditorFactory.getEditor(capability, option);
			
			if (input != null){
				input.create(optionsGroup,  showDefault);
			} else {
				// don't show options that don't have a defined view
				label.dispose();
				continue;
			}
			
			optionInputs.put(option, input);
			optionWidgets.add(input.getWidget());
			optionWidgets.add(label);
		}
		
		optionsGroup.layout(true);
	}
}
