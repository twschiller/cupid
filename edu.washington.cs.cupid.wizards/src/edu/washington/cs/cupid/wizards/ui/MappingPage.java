package edu.washington.cs.cupid.wizards.ui;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.DerivedCapability;

public class MappingPage extends WizardPage {

	//
	// Views
	//

	private Text objectType;
	private TreeViewer keyTree;
	private Combo keyLinkCombo;

	private TreeViewer valueTree;
	private Combo valueLinkCombo;
	
	//
	// Model
	//
	
	private boolean keyAsType = true;
	private TypeToken<?> keyType;
	private ICapability<?,?> keySet;
	
	private ICapability<?,?> valueSet;
	
	private ArrayList<Method> valueLinks;
	private ArrayList<Method> keyLinks;
	
	protected MappingPage() {
		super("Mapping");
	}

	@Override
	public void createControl(Composite parent) {
		setTitle("New Mapping");
		setMessage("Select a key and value collection followed by the fields to link");
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		composite.setLayout(layout);
		
		buildKeyTree(composite);
		buildValueTree(composite);
		
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
				valueLinks = links(getElementType(valueSet));
				
				valueLinkCombo.removeAll();
				for (Method m : valueLinks){
					valueLinkCombo.add(m.getName());
				}
			}
		});
	
		buildInjection(composite);
	
		setControl(composite);
	}
	
	
	private void updateKey(){
		if (keyAsType){
			keyType = typeFromString(objectType.getText());
		}else{
			keySet = selectedCapability(keyTree);
			keyType = getElementType(keySet);
		}
		
		keyLinkCombo.removeAll();
		keyLinks = Lists.newArrayList();
		
		if (keyType != null){
			keyLinks = links(keyType);
			
			for (Method m : keyLinks){
				keyLinkCombo.add(m.getName());
			}
		}
		
		valueTree.refresh();
	}
	
	private ICapability<?,?> selectedCapability(TreeViewer viewer){
		Object selected = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		
		if (selected instanceof ICapability){
			return (ICapability<?,?>) selected;
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
	
	private TypeToken<?> getElementType(ICapability<?,?> capability){
		ParameterizedType type = (ParameterizedType) capability.getReturnType().getType();
		Class<?> param = (Class<?>) type.getActualTypeArguments()[0];
		return TypeToken.of(param);
	}
	
	private void buildKeyTree(Composite composite){
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setText("Map Keys");
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 300;
		group.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		
		Composite selectRow = new Composite(group, SWT.NONE);
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
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		Composite objectRow = new Composite(group, SWT.NONE);
		GridData objectRowData = new GridData(GridData.FILL_HORIZONTAL);
		objectRow.setLayoutData(objectRowData);
		
		RowLayout objectRowLayout = new RowLayout();
		objectRowLayout.center = true;
		objectRow.setLayout(objectRowLayout);
		
		Label objectLabel = new Label(objectRow, SWT.LEFT);
		objectLabel.setText("Object Type:");
		
		objectType = new Text(objectRow, SWT.LEFT | SWT.BORDER);
		objectType.setText("java.lang.Object");
		RowData objectTypeData = new RowData();
		// TODO the control isn't grabbing the space properly
		data.grabExcessHorizontalSpace = true;
		objectType.setLayoutData(objectTypeData);
		
		objectType.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateKey();
			}
		});
		
		Button objectSelect = new Button(objectRow, SWT.PUSH);
		objectSelect.setText("Select");
		
		Label treeLabel = new Label(group, SWT.LEFT);
		treeLabel.setText("Capability:");
		
		keyTree = new TreeViewer(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		keyTree.setContentProvider(new TreeContentProvider());
		keyTree.setLabelProvider(new KeyTreeLabelProvider());
		
		TreeColumn column = new TreeColumn(keyTree.getTree(), SWT.LEFT);
		column.setWidth(300);
		column.setText("Capability");
		keyTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		keyTree.setInput(CupidPlatform.getCapabilityRegistry().getCapabilitiesForOutput(TypeToken.of(Collection.class)));
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
	
	private void buildValueTree(Composite composite){
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 300;
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
	
		valueTree.setInput(CupidPlatform.getCapabilityRegistry().getCapabilitiesForOutput(TypeToken.of(Collection.class)));
	}

	private void buildInjection(Composite composite){
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		group.setText("Mapping Function");
		group.setLayoutData(data);
		
		RowLayout layout = new RowLayout();
		layout.center = true;
		group.setLayout(layout);
		
		new Label(group, SWT.LEFT).setText("Where");
		keyLinkCombo = new Combo(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		new Label(group, SWT.LEFT).setText("of key equals");
		valueLinkCombo = new Combo(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		new Label(group, SWT.LEFT).setText("of value");
	}
	
	private class KeyTreeLabelProvider extends LabelProvider{
		@Override
		public String getText(Object element) {
			if (element instanceof ICapability){
				ICapability<?,?> capability = (ICapability<?,?>) element;
				return capability.getName() + ": " + capability.getDescription();
			}else if (element instanceof DerivedCapability){
				DerivedCapability capability = (DerivedCapability) element;
				return capability.getGetter().getName();
			}else{
				throw new RuntimeException("Unexpected tree element of type " + element.getClass());
			}
		}
	}

	private boolean isValidValue(ICapability<?,?> generator){
		if (keyAsType){
			return generator.getParameterType().equals(ICapability.UNIT_TOKEN);
		}else if (keySet != null){
			return CapabilityExecutor.isCompatible(generator, keySet.getParameterType());
		}else{
			return true;
		}
	}
	
	private class ValuesLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider{

		@Override
		public String getText(Object element) {
			if (element instanceof ICapability){
				ICapability<?,?> capability = (ICapability<?,?>) element;
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

			ICapability<?,?> capability = element instanceof ICapability 
					? (ICapability<?,?>) element
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
			Collection<ICapability<?,?>> xs = (Collection<ICapability<?,?>>) inputElement;
			return xs.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ICapability){
				return DerivedCapability.derivedProjections((ICapability<?,?>) parentElement).toArray();
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
			if (element instanceof ICapability){
				return !DerivedCapability.derivedProjections((ICapability<?,?>) element).isEmpty();
			}else{
				return false;
			}
		}
	}
}
