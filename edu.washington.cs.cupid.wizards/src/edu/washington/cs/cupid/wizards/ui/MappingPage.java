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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TreeColumn;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.wizards.internal.DerivedCapability;

public class MappingPage extends WizardPage {

	//
	// Views
	//

	private TreeViewer keyTree;
	private TreeViewer valueTree;
	private List keyLinkList;
	private List valueLinkList;
	
	//
	// Model
	//
	
	private ICapability<?,?> keySet;
	private ICapability<?,?> valueSet;
	private ArrayList<Method> keyLinks;
	private ArrayList<Method> valueLinks;
	
	
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
		
		Label keyLabel = new Label(composite, SWT.LEFT);
		keyLabel.setText("Key Set:");
		
		Label valueLabel = new Label(composite, SWT.LEFT);
		valueLabel.setText("Value Set:");
		
		buildKeyTree(composite);
		buildValueTree(composite);
		
		keyTree.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				if (selected instanceof ICapability){
					keySet = (ICapability<?,?>) selected;
				}else if (selected instanceof DerivedCapability){
					keySet = ((DerivedCapability) selected).toPipeline();
				}
				
				keyLinks = links(keySet);
				
				keyLinkList.removeAll();
				for (Method m : keyLinks){
					keyLinkList.add(m.getName());
				}
				
				valueTree.refresh();
			}
		});
		
		valueTree.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				if (selected instanceof ICapability){
					valueSet = (ICapability<?,?>) selected;
				}else if (selected instanceof DerivedCapability){
					valueSet = ((DerivedCapability) selected).toPipeline();
				}
				
				valueLinks = links(valueSet);
				
				valueLinkList.removeAll();
				for (Method m : valueLinks){
					valueLinkList.add(m.getName());
				}
			}
		});
		
		Label keyLinkLabel = new Label(composite, SWT.LEFT);
		keyLinkLabel.setText("Key Link:");
		
		Label valueLinkLabel = new Label(composite, SWT.LEFT);
		valueLinkLabel.setText("Value Link:");
		
		buildKeyLinkList(composite);
		buildValueLinkList(composite);
		
		setControl(composite);
	}
	
	private void buildKeyTree(Composite composite){
		keyTree = new TreeViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		keyTree.setContentProvider(new TreeContentProvider());
		keyTree.setLabelProvider(new KeyTreeLabelProvider());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 300;
		
		TreeColumn column = new TreeColumn(keyTree.getTree(), SWT.LEFT);
		column.setWidth(300);
		column.setText("Capability");
		keyTree.getTree().setLayoutData(data);
		
		keyTree.setInput(CupidPlatform.getCapabilityRegistry().getCapabilitiesForOutput(TypeToken.of(Collection.class)));
	}
	
	private static ArrayList<Method> links(ICapability<?,?> capability){
		ParameterizedType type = (ParameterizedType) capability.getReturnType().getType();
		Class<?> param = (Class<?>) type.getActualTypeArguments()[0];
		
		ArrayList<Method> result = Lists.newArrayList();
		
		for (Method method : param.getMethods()){
			if (DerivedCapability.isGetter(method)){
				result.add(method);
			}
		}
		
		return result;
	}
	
	private void buildValueTree(Composite composite){
		valueTree = new TreeViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		valueTree.setContentProvider(new TreeContentProvider());
		valueTree.setLabelProvider(new ValuesLabelProvider());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 300;
		
		TreeColumn column = new TreeColumn(valueTree.getTree(), SWT.LEFT);
		column.setWidth(300);
		column.setText("Capability");
		valueTree.getTree().setLayoutData(data);
	
		valueTree.setInput(CupidPlatform.getCapabilityRegistry().getCapabilitiesForOutput(TypeToken.of(Collection.class)));
	}
	
	private void buildKeyLinkList(Composite composite){
		keyLinkList = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 100;
		keyLinkList.setLayoutData(data);
	}
	
	private void buildValueLinkList(Composite composite){
		valueLinkList = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 100;
		valueLinkList.setLayoutData(data);
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
			if (keySet == null){
				return null;
			}else{
				ICapability capability = element instanceof ICapability 
						?  (ICapability) element
						:  ((DerivedCapability) element).getCapability();
				
				if (CapabilityExecutor.isCompatible(capability, keySet.getParameterType())){
					return null;
				}else{
					return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
				}	
			}
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
