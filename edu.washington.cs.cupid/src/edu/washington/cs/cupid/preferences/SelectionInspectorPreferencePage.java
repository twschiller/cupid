package edu.washington.cs.cupid.preferences;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
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

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.internal.CupidActivator;


public class SelectionInspectorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String SEPARATOR = ";;";
	
	private List<ICapability<?,?>> model;
	private Table table;
	
	private IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
	
	
	public SelectionInspectorPreferencePage() {
		setPreferenceStore(CupidActivator.getDefault().getPreferenceStore());
		setDescription("Cupid Selection Inspector Preference Page");
	}
	
	@Override
	public void init(IWorkbench workbench) {
	}
	
	@Override
	protected Control createContents(Composite parent) {
		
		Set<String> hidden = Sets.newHashSet(preferences.getString(PreferenceConstants.P_INSPECTOR_HIDE).split(SEPARATOR));
		
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
		label.setText("Active Capabilities:");
		
		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		
		//table.setHeaderVisible(true);
		
		String[] titles = { "Capability" };
		
		for (int i = 0 ; i < titles.length; i++){
			TableColumn column = new TableColumn(table, SWT.NULL);
			column.setText(titles[i]);
		}
	
		model = Lists.newArrayList(CupidPlatform.getCapabilityRegistry().getCapabilities());
		Collections.sort(model, new Comparator<ICapability<?,?>>(){
			@Override
			public int compare(ICapability<?, ?> lhs, ICapability<?, ?> rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		
		
		
		for (ICapability<?, ?> capability : model){
			TableItem item = new TableItem(table, SWT.NULL);
			item.setText(capability.getName());
			item.setText(0, capability.getName());

			if (!hidden.contains(capability.getUniqueId())){
				item.setChecked(true);
			}
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
	
	
	private void update(){
		List<String> hidden = Lists.newArrayList();
		
		for (int i = 0; i < table.getItemCount(); i++){
			if (!table.getItem(i).getChecked()){
				hidden.add(model.get(i).getUniqueId());
			}
		}
		
		preferences.setValue(PreferenceConstants.P_INSPECTOR_HIDE, Joiner.on(SEPARATOR).join(hidden));
	}
}