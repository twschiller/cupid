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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IParameter;
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;
import edu.washington.cs.cupid.wizards.internal.DerivedCapability;

/**
 * Wizard for creating a linear chain of capabilities
 * @author Todd Schiller
 */
public class CreatePipelinePage extends WizardPage{
	
	// TODO organization (alphabetize, or use unique id paths?)
	// TODO window icon
	
	// http://www.vogella.com/articles/EclipseDialogs/article.html#tutorialswt
	
	protected CreatePipelinePage() {
		super("Create pipeline");
	}

	private static final String DEFAULT_MESSAGE = "Select capabilities to form a pipeline.";
	
	//
	// Model
	//
	
	private List<ICapability> current = Lists.newLinkedList();
	
	//
	// View
	// 
	
	private TreeViewer capabilityTree;
	private TableViewer pipelineTable;
	
	private Text nameEntry;
	private Text descriptionEntry;
	
	@Override
	public void createControl(Composite parent) {
		this.setTitle("New Pipeline");
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
	
		createMetaGroup(composite);
		
		Label available = new Label(composite, SWT.LEFT);
		available.setText("Available Capabilities:");
		
		Label pipeline = new Label(composite, SWT.LEFT);
		pipeline.setText("Capability Pipeline:");
		
		buildCapabilityTree(composite);
		buildPipelineTable(composite);
		
		capabilityTree.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				if (selected instanceof ICapability){
					current.add((ICapability) selected);
				}else if (selected instanceof DerivedCapability){
					current.add(((DerivedCapability) selected).toPipeline());
				}
				
				pipelineTable.setInput(current);
				capabilityTree.refresh(true);
				refreshMessage();
			}
		});

		pipelineTable.getTable().addKeyListener(new DeleteListener());
		
		this.setMessage(DEFAULT_MESSAGE);
		
		setControl(composite);
	}

	private class DeleteListener implements KeyListener{
		@Override
		public void keyPressed(KeyEvent e) {
			// NO OP
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (!pipelineTable.getSelection().isEmpty()){
				IStructuredSelection selected = ((IStructuredSelection) pipelineTable.getSelection());
				ICapability element = (ICapability) selected.getFirstElement();
				
				int index = -1;
				for (int i = 0; i < current.size(); i++){
					if (current.get(i) == element){
						index = i;
					}
				}
				if (index < 0){
					return;
				}
				
				switch (e.keyCode){
				case SWT.DEL:
				case SWT.BS:
					current.remove(index);
					pipelineTable.setInput(current);
					pipelineTable.getTable().select(index == current.size() ? current.size()-1 : index);
					break;
				}
				
				refreshMessage();
				capabilityTree.refresh();
			}
		}
	}
	
	private void refreshMessage(){
		List<String> errors = typeErrors();
		if (current.isEmpty()){
			this.setMessage(DEFAULT_MESSAGE);
			this.setPageComplete(false);
		}else if (errors.isEmpty()){
			this.setMessage("Capability is well-typed.");
			this.setPageComplete(true);
		}else{
			this.setErrorMessage(errors.get(0));
			this.setPageComplete(false);
		}
	}
	
	private class TableContentProvider implements IStructuredContentProvider{

		@Override
		public void dispose() {
			// NO OP
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// NO OP (use instance list)
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return current.toArray();
		}
	}
	
	
	private class TableLabelProvider implements ITableLabelProvider{

		@Override
		public void addListener(ILabelProviderListener listener) {
			// NO OP
		}

		@Override
		public void dispose() {
			// NO OP
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// NO OP
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ICapability capability = (ICapability) element;
			switch(columnIndex){
			case 0:
				return capability.getName();
			case 1:
				return TypeManager.simpleTypeName(CapabilityUtil.unaryParameter(capability).getType().getType());
			case 2:
				return TypeManager.simpleTypeName(CapabilityUtil.singleOutput(capability).getType().getType());
			default:
				return null;
			}
		}	
	}
	
	/**
	 * Returns list of type error messages for current pipeline
	 * @return list of type error messages
	 */
	private List<String> typeErrors(){
		List<String> result = Lists.newArrayList();
		
		if (!current.isEmpty()){
			
			TypeToken<?> last = CapabilityUtil.singleOutput(current.get(0)).getType();
			
			for (int i = 1; i < current.size(); i++){
				ICapability next = current.get(i);
				IParameter<?> nextParameter = CapabilityUtil.unaryParameter(next);
				if (!TypeManager.isCompatible(nextParameter, last)){
					result.add("Capability " + next.getName() + " with input " + TypeManager.simpleTypeName(nextParameter.getType()) + " is not compatible with type " + TypeManager.simpleTypeName(last));
				}
				last = CapabilityUtil.singleOutput(next).getType();
			}
		}
		
		return result;
	}
	
	private class TreeLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider{

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
			if (current.isEmpty()){
				return null;
			}else{
				ICapability capability = element instanceof ICapability 
						?  (ICapability) element
						:  ((DerivedCapability) element).getCapability();
				
				ICapability last = current.get(current.size()-1);
						
				if (TypeManager.isCompatible(CapabilityUtil.unaryParameter(capability), CapabilityUtil.singleOutput(last).getType())){
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
			List<ICapability> xs = Lists.newArrayList((Collection<ICapability>) inputElement);
			Collections.sort(xs, new Comparator<ICapability>(){
				@Override
				public int compare(ICapability lhs, ICapability rhs) {
					return lhs.getName().compareTo(rhs.getName());
				}
				
			});
			return xs.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ICapability){
				List<DerivedCapability> xs = DerivedCapability.derived((ICapability) parentElement);
				Collections.sort(xs, new Comparator<DerivedCapability>(){
					@Override
					public int compare(DerivedCapability lhs, DerivedCapability rhs) {
						return lhs.getGetter().getName().compareTo(rhs.getGetter().getName());
					}
				});
				return xs.toArray();
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
				return !DerivedCapability.derived((ICapability) element).isEmpty();
			}else{
				return false;
			}
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
		nameEntry.setText("My Capability");
		
		Label descriptionLabel = new Label(metaGroup, SWT.LEFT);
		descriptionLabel.setText("Description:");
		
		descriptionEntry = new Text(metaGroup, SWT.SINGLE | SWT.BORDER);
		descriptionEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		metaGroup.pack();
	}
	
	public int getPipelineLength(){
		return current.size();
	}
	
	public DynamicSerializablePipeline createPipeline(){
		List<Object> descriptors = Lists.newArrayList();
		for (Object x : current){
			if (x instanceof Serializable){
				descriptors.add(x);
			}else{
				descriptors.add(((ICapability)x).getUniqueId());
			}
		}
		
		DynamicSerializablePipeline pipeline = new DynamicSerializablePipeline(
				nameEntry.getText(),
				descriptionEntry.getText(),
				descriptors);
		
		return pipeline;
	}
	
	private void buildCapabilityTree(Composite composite){
		capabilityTree = new TreeViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		capabilityTree.setContentProvider(new TreeContentProvider());
		capabilityTree.setLabelProvider(new TreeLabelProvider());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 400;
		
		TreeColumn column = new TreeColumn(capabilityTree.getTree(), SWT.LEFT);
		column.setWidth(300);
		column.setText("Capability");
		capabilityTree.getTree().setLayoutData(data);
		
		SortedSet<ICapability> linear = CupidPlatform.getCapabilityRegistry().getCapabilities(new Predicate<ICapability>(){
			@Override
			public boolean apply(ICapability capability) {
				return CapabilityUtil.isLinear(capability);
			}
		});
		
		capabilityTree.setInput(Lists.newArrayList(linear));
	}
	
	private void buildPipelineTable(Composite composite){
		pipelineTable = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data = new GridData(GridData.FILL_BOTH);
		data.minimumWidth = 300;
		
		pipelineTable.setContentProvider(new TableContentProvider());
		pipelineTable.setLabelProvider(new TableLabelProvider());
		pipelineTable.getTable().setLayoutData(data);
		
		TableColumn nameColumn = new TableColumn(pipelineTable.getTable(), SWT.LEFT);
		nameColumn.setText("Capability");
		nameColumn.setWidth(200);
		
		TableColumn inputTypeColumn = new TableColumn(pipelineTable.getTable(), SWT.LEFT);
		inputTypeColumn.setText("Input Type");
		inputTypeColumn.setWidth(100);
		
		TableColumn outputTypeColumn = new TableColumn(pipelineTable.getTable(), SWT.LEFT);
		outputTypeColumn.setText("Output Type");
		outputTypeColumn.setWidth(100);
		
		pipelineTable.getTable().setHeaderVisible(true);
	}

	
}
