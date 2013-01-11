package edu.washington.cs.cupid.preferences;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.views.ViewRule;


public class TypeViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	// TODO support cases where capability no longer exists
	
	private static final String DEFAULT_TYPE = "java.lang.Object";
	private static final int ROW_HEIGHT = 20;
	
	private Table table;
	private Gson gson = new Gson();
	
	private Map<Combo, Map<String, ICapability<?,?>>> comboData = Maps.newHashMap();
	
	private IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
	
	public TypeViewPreferencePage() {
		setPreferenceStore(CupidActivator.getDefault().getPreferenceStore());
		setDescription("Cupid Selection Type Views Preference Page");
	}

	@Override
	public void init(IWorkbench workbench) {
	}
	
	@Override
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginRight = 5;
		layout.marginTop = 5;
		layout.marginWidth  = 0;
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		makeAddRuleBox(composite);
		makeRuleTable(composite);
			
		return null;
	}
	
	private void makeRuleTable(Composite parent){
		List<ViewRule> rules = gson.fromJson(
				preferences.getString(PreferenceConstants.P_TYPE_VIEWS),
				new com.google.gson.reflect.TypeToken<List<ViewRule>>(){}.getType());
		
		table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		
		table.setHeaderVisible(true);
		
		TableLayout layout = new TableLayout();
		layout.addColumnData( new ColumnWeightData(1));
		layout.addColumnData( new ColumnWeightData(1));
		layout.addColumnData( new ColumnWeightData(2));
		layout.addColumnData( new ColumnWeightData(1));
		table.setLayout(layout);
		
		TableColumn typeColumn = new TableColumn(table, SWT.NULL);
		typeColumn.setText("Type");
		
		TableColumn qualifiedColumn = new TableColumn(table, SWT.NULL);
		qualifiedColumn.setText("Qualified Name");
		
		TableColumn viewColumn = new TableColumn(table, SWT.NULL);
		viewColumn.setText("Capability");
			
		TableColumn deleteColumn = new TableColumn(table, SWT.NULL);
		deleteColumn.setText("Remove");
				
		populateTable(rules);
	
		
		try{
			Method setItemHeightMethod = table.getClass().getDeclaredMethod("setItemHeight", int.class);
			setItemHeightMethod.setAccessible(true);
			setItemHeightMethod.invoke(table, ROW_HEIGHT);
		}catch(Exception ex){
			// TODO log error
			// NO OP
		}
	}
		
	private void populateTable(List<ViewRule> rules){
		for (ViewRule rule : rules){
			addRule(rule);
		}
	}
	
	private ICapability<?,?> capability(ViewRule rule){
		ICapability<?,?> result = null;
		
		if (rule.getCapability() != null){
			try {
				result = CupidPlatform.getCapabilityRegistry().findCapability(rule.getCapability());
			} catch (NoSuchCapabilityException e) {
				// NO OP
			}
		}
		return result;
	}
	
	private void populateComboBox(Combo combo, ViewRule rule){
		Class<?> clazz = null;
		
		try{
			clazz = Class.forName(rule.getQualifiedType(), false, CupidActivator.class.getClassLoader());
		}catch(Exception ex){
			CupidActivator.getDefault().logError("Cannot load type for view preference: " + rule.getQualifiedType(), ex);
		}			
		
		if (clazz != null){
			Set<ICapability<?,?>> xs = CupidPlatform.getCapabilityRegistry().getCapabilities(TypeToken.of(clazz), TypeToken.of(String.class));
			Map<String, ICapability<?,?>> forType = Maps.newHashMap();
			
			for (ICapability<?,?> capability : xs){
				combo.add(capability.getName());
				forType.put(capability.getName(), capability);
			}
			
			comboData.put(combo, forType);
		}
	}
	
	private void addRule(ViewRule rule){
		final TableItem item = new TableItem(table, SWT.NONE);
		
		ICapability<?,?> capability = capability(rule);
		item.setData(capability);
		
		item.setText(new String[]{
			rule.getQualifiedType().substring(rule.getQualifiedType().lastIndexOf('.')+1),
			rule.getQualifiedType(),
			capability  == null ? "" : capability.getName()
		});
		
		item.setChecked(rule.isActive());
		
		TableEditor editor = new TableEditor(table);
		final Combo combo = new Combo(table, SWT.DROP_DOWN);
		editor.grabHorizontal = true;
		editor.setEditor(combo, item, 2);
		populateComboBox(combo, rule);
		
		if (capability != null){
			combo.setText(capability.getName());
		}
		
		combo.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				item.setData(comboData.get(combo).get(combo.getText()));
			}
		});
		
		editor = new TableEditor(table);
		final Button remove = new Button(table, SWT.PUSH);
		remove.setText("Remove");
		editor.grabHorizontal = true;
		editor.minimumWidth = remove.getSize().x;
		editor.setEditor(remove, item, 3);
		
		remove.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.remove(table.indexOf(item));
				remove.dispose();
				combo.dispose();
				table.layout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
	}
	
	private void makeAddRuleBox(Composite parent){
		Composite ruleBox = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		ruleBox.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		ruleBox.setLayout(layout);
		
		final Text typeBox = new Text(ruleBox, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		typeBox.setLayoutData(data);
		typeBox.setText(DEFAULT_TYPE);
		typeBox.setEnabled(false);
		
		Button searchButton = new Button(ruleBox, SWT.PUSH);
		searchButton.setText("Search");
		
		searchButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] types = showTypeDialog();
				if (types != null && types.length > 0){
					typeBox.setText(((IType)types[0]).getFullyQualifiedName());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
			
		});
		
		final Button addRule = new Button(ruleBox, SWT.PUSH);
		addRule.setText("Add");
		
		addRule.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				addRule(new ViewRule(typeBox.getText(), null, true));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});	
	}
	
	private Object[] showTypeDialog(){
		SelectionDialog dialog;
		try {
			dialog = JavaUI.createTypeDialog(this.getShell(), 
					null,
					SearchEngine.createWorkspaceScope(),
					IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
					false);
		} catch (JavaModelException e) {
			return null;
			// NO OP
		}
		dialog.open();

		return dialog.getResult();
	}

	@Override
	protected void performApply() {
		List<ViewRule> rules = Lists.newArrayList();
		
		for (int i = 0; i < table.getItemCount(); i++){
			TableItem item = table.getItem(i);
			
			rules.add(new ViewRule(
					item.getText(1),
					item.getData() == null ? null : ((ICapability<?,?>) item.getData()).getUniqueId(),
					item.getChecked()));
		}
		
		preferences.setValue(PreferenceConstants.P_TYPE_VIEWS, gson.toJson(rules));
	}

	@Override
	protected void performDefaults() {
		table.removeAll();
	}
	
}