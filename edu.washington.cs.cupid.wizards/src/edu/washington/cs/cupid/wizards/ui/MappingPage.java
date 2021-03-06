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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.SortedSet;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityArguments;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.ISerializableCapability;
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;
import edu.washington.cs.cupid.wizards.TypeComboUpdater;
import edu.washington.cs.cupid.wizards.TypeUtil;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.CapabilityMapping;
import edu.washington.cs.cupid.wizards.internal.DerivedCapability;
import edu.washington.cs.cupid.wizards.internal.ValueMapping;

public class MappingPage extends WizardPage {

	private static final String DEFAULT_NAME = "My Mapping";
	private static final String DEFAULT_DESCRIPTION = "A mapping between values";
	private static final String VALUE_SENTINAL = "<Value>";
	private static final String DEFAULT_KEY_TYPE = "java.lang.Object";
	
	//
	// Views
	//
	
	private Text nameEntry;
	private Text descriptionEntry;
	
	private Combo objectType;
	private TreeViewer keyTree;
	private Combo keyLinkCombo;

	private TreeViewer valueTree;
	private Combo valueLinkCombo;
	
	private Button objectSelect;
	
	private Group typeGroup;
	private Group capabilityGroup;
	
	//
	// Model
	//
	
	private boolean keyAsType = true;
	private TypeToken<?> keyType;
	
	private ISerializableCapability keySet;
	private ISerializableCapability valueSet;
	
	private ArrayList<Method> valueLinks;
	private ArrayList<Method> keyLinks;
	
	protected MappingPage(){
		super("Mapping");
	}
	
	protected MappingPage(TypeToken<?> startingType) {
		super("Mapping");
		this.keyType = startingType;
	}

	@Override
	public void createControl(Composite parent) {
		setTitle("New Mapping");
		setMessage("Select a key and value collection followed by the fields to link");
		
		Composite dialog = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		dialog.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		dialog.setLayout(layout);
		
		createMetaGroup(dialog);
		
		Composite selectRow = new Composite(dialog, SWT.NONE);
		
		GridData selectRowData = new GridData();
		selectRowData.horizontalAlignment = SWT.CENTER;
		selectRow.setLayoutData(selectRowData);
		
		RowLayout selectRowLayout = new RowLayout();
		selectRowLayout.center = true;
		selectRow.setLayout(selectRowLayout);
		
		Label useLabel = new Label(selectRow, SWT.LEFT);
		useLabel.setText("Key Type:");
		
	    Button useType = new Button(selectRow, SWT.RADIO);
		useType.setSelection(true);
		useType.setText("Object");
		useType.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyAsType = true;
				updateKey();
				enableKeys();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		Button useCapability = new Button(selectRow, SWT.RADIO);
		useCapability.setText("Capability Output");
		useCapability.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyAsType = false;
				updateKey();
				enableKeys();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});

		createValueGroup(dialog);
		createKeyGroup(dialog);
		
		keyTree.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateKey();
			}
		});
		
		valueTree.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				valueSet = selectedCapability(valueTree);

				valueLinkCombo.removeAll();
				valueLinkCombo.add(VALUE_SENTINAL);
				
				if (valueSet != null){
					valueLinks = links(getElementType(valueSet));

					for (Method m : valueLinks){
						valueLinkCombo.add(m.getName());
					}
				}
				
				valueLinkCombo.setText(VALUE_SENTINAL);
			}
		});
	
		createInjectionGroup(dialog);
		enableKeys();
		updateKey();
		setControl(dialog);
	}
	
	private void enableKeys(){
		objectType.setEnabled(keyAsType);
		objectSelect.setEnabled(keyAsType);
		typeGroup.setEnabled(keyAsType);
		
		keyTree.getTree().setEnabled(!keyAsType);
		capabilityGroup.setEnabled(!keyAsType);
	}
	
	private void updateKey(){
		if (keyAsType){
			keyType = typeFromString(objectType.getText());
		}else{
			keySet = selectedCapability(keyTree);
			keyType = keySet == null ? null : getElementType(keySet);
		}
		
		keyLinkCombo.removeAll();
		keyLinks = Lists.newArrayList();
		
		keyLinkCombo.add(VALUE_SENTINAL);
		
		if (keyType != null){
			keyLinks = links(keyType);
			
			for (Method m : keyLinks){
				keyLinkCombo.add(m.getName());
			}
		}
		
		keyLinkCombo.setText(VALUE_SENTINAL);
		
		valueTree.refresh();
	}
	
	/**
	 * Returns the capability selected from the capability listing; returns <tt>null</tt>
	 * if no element is selected.
	 * @param viewer the capability viewer
	 * @return the capability selected from the capability listing, or <tt>null</tt> if no element is selected
	 */
	private ISerializableCapability selectedCapability(TreeViewer viewer){
		Object selected = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
	
		if (selected == null){
			return null;
		}else if (selected instanceof ISerializableCapability){
			return (ISerializableCapability) selected;
		}else if (selected instanceof ICapability){
			return new DynamicSerializablePipeline(
					null, null, 
					Lists.<Serializable>newArrayList(((ICapability) selected).getName()),
					Lists.<ICapabilityArguments>newArrayList(CapabilityArguments.NONE));
		}else if (selected instanceof DerivedCapability){
			return ((DerivedCapability) selected).toPipeline();
		}else{
			throw new RuntimeException("Unexpected tree entry");
		}
	}
	
	private TypeToken<?> typeFromString(String data){
		Class<?> clazz;
		try {
			clazz = Activator.getDefault().getBundle().loadClass(data);
			return TypeToken.of(clazz);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	private TypeToken<?> getElementType(ICapability capability){
		if (capability == null){
			throw new NullPointerException("capability cannot be null");
		}
		ParameterizedType type = (ParameterizedType) CapabilityUtil.singleOutput(capability).getType().getType();
		Class<?> param = (Class<?>) type.getActualTypeArguments()[0];
		return TypeToken.of(param);
	}
	
	private void createTypeKeyGroup(Composite composite){
	    typeGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
	    GridData data = new GridData(GridData.FILL_HORIZONTAL);
		typeGroup.setLayoutData(data);
	    typeGroup.setText("Map Key");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		typeGroup.setLayout(layout);
		
		Label objectLabel = new Label(typeGroup, SWT.LEFT);
		objectLabel.setText("Object Type:");
		
		objectType = new Combo(typeGroup, SWT.LEFT | SWT.BORDER);
		objectType.setText(keyType != null ? keyType.toString() : DEFAULT_KEY_TYPE);
		objectType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final TypeComboUpdater updater = new TypeComboUpdater(objectType);
		
		objectType.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateKey();
			}
		});
		
	    objectSelect = new Button(typeGroup, SWT.PUSH);
		objectSelect.setText("Search");
		objectSelect.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IType selected = TypeUtil.showTypeDialog(getShell());
					if (selected != null){
						String newType = selected.getFullyQualifiedName();
						objectType.setText(newType);
						updater.updateSuperTypeList(newType);
					}
				} catch (JavaModelException ex) {
					throw new RuntimeException("Error opening type search dialog", ex);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
	}
	
	private void createCapabilityKeyGroup(Composite composite){
		capabilityGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 200;
		capabilityGroup.setLayoutData(data);
		capabilityGroup.setText("Map Key");
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		capabilityGroup.setLayout(layout);
		
		keyTree = new TreeViewer(capabilityGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		keyTree.setContentProvider(new TreeContentProvider());
		keyTree.setLabelProvider(new KeyTreeLabelProvider());
		
		TreeColumn column = new TreeColumn(keyTree.getTree(), SWT.LEFT);
		column.setWidth(300);
		column.setText("Capability");
		keyTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		keyTree.setInput(validCapabilities());

	}
	
	private void createKeyGroup(Composite composite){
		createTypeKeyGroup(composite);
		createCapabilityKeyGroup(composite);
	}
	
	private static ArrayList<Method> links(TypeToken<?> type){
		ArrayList<Method> result = Lists.newArrayList();
		
		for (Method method : type.getRawType().getMethods()){
			if (DerivedCapability.isGetter(method)){
				result.add(method);
			}
		}
		
		return result;
	}
	
	private LinkedHashSet<ICapability> validCapabilities(){
		SortedSet<ICapability> consistent = CupidPlatform.getCapabilityRegistry().getCapabilitiesForOutput(TypeToken.of(Collection.class));
		
		SortedSet<ICapability> linear = CupidPlatform.getCapabilityRegistry().getCapabilities(new Predicate<ICapability>(){
			@Override
			public boolean apply(ICapability capability) {
				return CapabilityUtil.isLinear(capability);
			}
		});
		
		return Sets.newLinkedHashSet(Sets.intersection(consistent, linear));
	}
	
	private void createValueGroup(Composite composite){
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 300;
		data.verticalSpan = 3;
		group.setText("Map Values");
		group.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		
		valueTree = new TreeViewer(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		valueTree.setContentProvider(new TreeContentProvider());
		valueTree.setLabelProvider(new ValuesLabelProvider());
	
		TreeColumn column = new TreeColumn(valueTree.getTree(), SWT.LEFT);
		column.setWidth(300);
		column.setText("Capability");
		valueTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
	
		valueTree.setInput(validCapabilities());
	}

	private void createInjectionGroup(Composite composite){
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		group.setText("Mapping Function");
		group.setLayoutData(data);
	
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		group.setLayout(layout);
		
		new Label(group, SWT.LEFT).setText("Where");
		keyLinkCombo = new Combo(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		keyLinkCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		keyLinkCombo.add(VALUE_SENTINAL);
		keyLinkCombo.setText(VALUE_SENTINAL);
		
		new Label(group, SWT.LEFT).setText("of key equals");
		valueLinkCombo = new Combo(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		valueLinkCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
		valueLinkCombo.add(VALUE_SENTINAL);
		valueLinkCombo.setText(VALUE_SENTINAL);
		
		new Label(group, SWT.LEFT).setText("of value");
	}
	
	private class KeyTreeLabelProvider extends LabelProvider{
		@Override
		public String getText(Object element) {
			if (element instanceof ICapability){
				ICapability capability = (ICapability) element;
				return capability.getName() + ": " + capability.getDescription();
			}else if (element instanceof DerivedCapability){
				DerivedCapability capability = (DerivedCapability) element;
				return capability.getGetter().getName();
			}else{
				throw new RuntimeException("Unexpected tree element of type " + element.getClass());
			}
		}
	}

	/**
	 * Returns <tt>true</tt> iff the given value generator capability is compatible with the currently
	 * select key object or capability. The capability is valid if it is a generator, or if it has a single
	 * required input that is compatible with the input type of the key generator.
	 * @param generator the generator capability
	 * @return <tt>true</tt> iff the given value generator capability is compatible with the currently
	 * select key object or capability
	 */
	private boolean isValidValue(ICapability generator){
		if (keyAsType){
			return CapabilityUtil.isGenerator(generator);
		}else if (keySet != null){
			if (CapabilityUtil.isGenerator(keySet)){
				return CapabilityUtil.isGenerator(generator);
			}else{
				return CapabilityUtil.isGenerator(generator) || 
						   TypeManager.isCompatible(CapabilityUtil.unaryParameter(generator), CapabilityUtil.unaryParameter(keySet).getType());		
			}
		}else{
			return true;
		}
	}
	
	private void createMetaGroup(Composite parent){
		Group metaGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		metaGroup.setText("Meta Information");
		metaGroup.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		metaGroup.setLayout(layout);
		
		Label nameLabel = new Label(metaGroup, SWT.LEFT);
		nameLabel.setText("Name:");
		
		nameEntry = new Text(metaGroup, SWT.SINGLE | SWT.BORDER);
		nameEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameEntry.setText(DEFAULT_NAME);
		
		Label descriptionLabel = new Label(metaGroup, SWT.LEFT);
		descriptionLabel.setText("Description:");
		
		descriptionEntry = new Text(metaGroup, SWT.SINGLE | SWT.BORDER);
		descriptionEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionEntry.setText(DEFAULT_DESCRIPTION);
		
		metaGroup.pack();
	}
	
	private class ValuesLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider{

		@Override
		public String getText(Object element) {
			if (element instanceof ICapability){
				ICapability capability = (ICapability) element;
				return capability.getName() + ": " + capability.getDescription();
			}else if (element instanceof DerivedCapability){
				DerivedCapability capability = (DerivedCapability) element;
				return capability.getGetter().getName();
			}else{
				throw new RuntimeException("Unexpected tree element of type " + element.getClass());
			}
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			ICapability capability = element instanceof ICapability 
					? (ICapability) element
					: ((DerivedCapability) element).getCapability();

			return isValidValue(capability) 
					? null 
					: Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);

		}


		@Override
		public Color getBackground(Object element, int columnIndex) {
			// USE DEFAULT
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0){
				return getText(element);
			}else{
				return null;
			}
		}
		
	}
	
	
	private class TreeContentProvider implements ITreeContentProvider{

		@Override
		public void dispose() {
			// NO OP
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			Collection<ICapability> xs = (Collection<ICapability>) inputElement;
			return xs.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ICapability){
				ICapability c = (ICapability) parentElement;
				
				if (CapabilityUtil.hasSingleOutput(c)){
					return DerivedCapability.derivedProjections(c, CapabilityUtil.singleOutput(c)).toArray();
				}else{
					return DerivedCapability.derived((ICapability) parentElement).toArray();			
				}
				
			}else if (parentElement instanceof DerivedCapability){
				DerivedCapability c = (DerivedCapability) parentElement;
				
				return c.getGetter() == null ?
						DerivedCapability.derivedProjections(c.getCapability(), c.getOutput()).toArray() :
						new Object[]{};
			}else{
				return new Object[]{};
			}
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof DerivedCapability){
				return ((DerivedCapability) element).getCapability();
			}else{
				return null;
			}
		}

		@Override
		public boolean hasChildren(Object element) {
			// TODO: make efficient
			return getChildren(element).length > 0;
		}
	}
	
	private String pullComboLink(Combo box){
		return box.getText().equals(VALUE_SENTINAL) ? null : box.getText();
		
	}
	
	public boolean hasKeyAsType(){
		return keyAsType;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ValueMapping<?,?> getValueMapping(){
		String name = nameEntry.getText();
		String description = descriptionEntry.getText();
		
		return new ValueMapping(name, description,
				keyType, pullComboLink(keyLinkCombo),
				valueSet, CapabilityUtil.singleOutput(valueSet).getType(), pullComboLink(valueLinkCombo));
		
	}
	
	public CapabilityMapping<?,?,?> getCapabilityMapping(){
		String name = nameEntry.getText();
		String description = descriptionEntry.getText();
		
		return new CapabilityMapping(name, description,
				CapabilityUtil.isGenerator(keySet) ? TypeToken.of(Void.class) : CapabilityUtil.unaryParameter(keySet).getType(),
				keySet, CapabilityUtil.singleOutput(keySet).getType(), pullComboLink(keyLinkCombo),
				valueSet, CapabilityUtil.singleOutput(valueSet).getType(), pullComboLink(valueLinkCombo));
	}
}
