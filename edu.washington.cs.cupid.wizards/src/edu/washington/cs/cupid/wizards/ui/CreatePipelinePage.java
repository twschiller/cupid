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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityArguments;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IOutput;
import edu.washington.cs.cupid.capability.ICapability.IParameter;
import edu.washington.cs.cupid.capability.OutputSelector;
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.standard.Exceptional;
import edu.washington.cs.cupid.standard.Identity;
import edu.washington.cs.cupid.views.OptionEditorFactory;
import edu.washington.cs.cupid.views.OptionEditorFactory.OptionEditor;
import edu.washington.cs.cupid.views.OptionEditorFactory.ValueChangedListener;
import edu.washington.cs.cupid.views.ReportWidget;
import edu.washington.cs.cupid.wizards.internal.DerivedCapability;
import edu.washington.cs.cupid.wizards.internal.LiftedCapability;

/**
 * Wizard for creating a linear chain of capabilities
 * @author Todd Schiller
 */
public class CreatePipelinePage extends WizardPage{
	
	private static final String DEFAULT_MESSAGE = "Select capabilities to form a pipeline.";
	
	//
	// Model
	//
	
	/**
	 * The current pipeline
	 */
	private List<ICapability> current = Lists.newLinkedList();
	
	/**
	 * Options for the selected capability that is in the pipeline
	 */
	private List<CapabilityArguments> currentOptions = Lists.newArrayList();;
	
	/**
	 * Options for the selected capability that has not been added yet
	 */
	private CapabilityArguments newOptions = new CapabilityArguments();
	
	//
	// View
	// 
	
	private Object selection = null;
	
	private TreeViewer capabilityTree;
	private TableViewer pipelineTable;
	
	private Text nameEntry;
	private Text descriptionEntry;
	private Text search;
	private Button hideInvalid;
	
	private Group optionGroup;
	private ScrolledComposite optionContainer;
	private Composite optionInnerContainer;
    private List<Widget> optionWidgets = Lists.newArrayList();
    private BiMap<IParameter<?>, OptionEditor<?>> optionInputs = HashBiMap.create();
    
    private InvalidFilter invalidFilter = new InvalidFilter();
    
	private Object [] previewInput = null;
    
	private Group previewGroup;
	private ReportWidget previewWidget;
	private TypeToken<?> inputType;
	
	// http://www.vogella.com/articles/EclipseDialogs/article.html#tutorialswt
	
	public CreatePipelinePage() {
		this(null, null);
	}
	
	protected CreatePipelinePage(TypeToken<?> inputType, Object [] previewInput) {
		super("Create pipeline");
		this.inputType = inputType;
		
		// Trim so that updates don't take too long
		if (previewInput != null){
			this.previewInput = new Object[Math.min(previewInput.length, 10)];
			
			for (int i = 0; i < this.previewInput.length; i++){
				this.previewInput[i] = previewInput[i];
			}
		}
	}
    
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
		
		createCapabilityToolbar(composite);
		
		Label pipeline = new Label(composite, SWT.LEFT);
		pipeline.setText("Capability Pipeline:");
		
		buildCapabilityTree(composite);
		buildPipelineTable(composite);
		createPreviewGroup(composite);
		buildOptionEditor(composite);

		capabilityTree.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				if (selected instanceof ICapability){
					if (!CapabilityUtil.hasSingleOutput((ICapability) selected)){
						return;
					}
					
					ICapability c = (ICapability) selected;
					
					if (current.size() > 0 &&
						isListCompatible(c, CapabilityUtil.singleOutput(current.get(current.size() - 1)).getType())){
					
						current.add(new LiftedCapability(c));
						
					}else{
						current.add(c);
					}
				}else if (selected instanceof DerivedCapability){
					current.add(((DerivedCapability) selected).toPipeline());
				}
				
				currentOptions.add(newOptions);
				
				pipelineTable.setInput(current);
				capabilityTree.refresh(true);
				
				refreshPreview();
				
				refreshMessage();
				
				search.setText("");
			}
		});
		
		capabilityTree.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				if (selection != selected){
					clearOptions();
					
					ICapability capability = null;
					if (selected instanceof ICapability){
						capability = (ICapability) selected;
					}else if (selected instanceof DerivedCapability){
						capability = ((DerivedCapability) selected).toPipeline();
					}
					
					showOptions(capability, null);
				
					selection = selected;
				}
			}
		});

		pipelineTable.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				
				if (selected == null){
					clearOptions();
				}else{
					showOptions((ICapability) selected, pipelineTable.getTable().getSelectionIndex());
				}
			}
		});
		
		pipelineTable.getTable().addKeyListener(new DeleteListener());
		
		this.setMessage(DEFAULT_MESSAGE);
		
		capabilityTree.addFilter(invalidFilter);
			
		setControl(composite);
	}

	private void createCapabilityToolbar(Composite container){
		Composite toolbar = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		toolbar.setLayout(layout);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label available = new Label(toolbar, SWT.LEFT);
		available.setText("Available Capabilities:");
		available.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		
		search = new Text(toolbar, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		search.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		hideInvalid = new Button(toolbar, SWT.CHECK);
		hideInvalid.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		hideInvalid.setText("Hide Incompatible");
		hideInvalid.setSelection(true);
		
		
		search.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				if (nameFilter != null){
					capabilityTree.removeFilter(nameFilter);
				}
				nameFilter = new NameFilter(search.getText());
				capabilityTree.addFilter(nameFilter);
			}
		});
		
		hideInvalid.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (hideInvalid.getSelection()){
					capabilityTree.addFilter(invalidFilter);
				}else{
					capabilityTree.removeFilter(invalidFilter);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NOP
			}
		});
	}
	
	private NameFilter nameFilter;
	
	private static class NameFilter extends ViewerFilter{
		private String query;
		
		private NameFilter(String query){
			this.query = query;
		}
		
		@Override
		public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
			if (query.isEmpty()){
				return elements;
			}else if (parent instanceof ICapability){
				// we're looking at derived capabilities
				return super.filter(viewer, parent, elements);
			}else{
				List<Object> result = Lists.newArrayList();
				
				for (Object elt : elements){
					ICapability capability = (ICapability) elt;
					
					if (capabilityMatches(capability)){
						result.add(capability);
					}else{
						for (Object o : getDerived(capability)){
							// a child matches
							if (select(viewer, capability, o)){
								result.add(capability);
								break;
							}
						}
					}
				}
				
				// we're looking at capabilities
				return result.toArray();
			}
		}

		
		private boolean capabilityMatches(ICapability capability){
			return isMatch(capability.getName()) || 
				   isMatch(capability.getDescription());
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof ICapability){
				ICapability capability = (ICapability) element;
				return capabilityMatches(capability);
			}else if (element instanceof DerivedCapability){
				// include if parent matched or getter matches
				
				DerivedCapability derived = (DerivedCapability) element;
				
				if (derived.getGetter() != null){
					return capabilityMatches(derived.getCapability()) || 
						   isMatch(derived.getGetter().getName());
				}else{
					return capabilityMatches(derived.getCapability()) || 
						   isMatch(derived.getOutput().getName());
				}
			}else{
				throw new IllegalArgumentException("Unexpected element of type " + element.getClass());
			}
		}
		
		private boolean isMatch(String text){
			return text.toUpperCase().contains(query.toUpperCase());
		}
	}
	
	private boolean canAttach(ICapability capability){
	
		
		if (current.size() == 0 && inputType != null){
			if (CapabilityUtil.isGenerator(capability)
					|| TypeManager.isCompatible(CapabilityUtil.unaryParameter(capability), inputType)
					|| isListCompatible(capability, inputType))
			{
				return true;
			}else{
				return false;
			}	
		} else if (current.isEmpty()){
			return true;
		}else{

			try{
				TypeToken<?> outputType = CapabilityUtil.singleOutput(createPipeline()).getType();

				if (CapabilityUtil.isGenerator(capability)
						|| TypeManager.isCompatible(CapabilityUtil.unaryParameter(capability), outputType)
						|| isListCompatible(capability, outputType))
				{
					return true;
				}else{
					return false;
				}	
			}catch(Exception ex){
				return false;
			}
		}
	}
	
	private class InvalidFilter extends ViewerFilter{
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			ICapability capability = element instanceof ICapability 
					?  (ICapability) element
					:  ((DerivedCapability) element).getCapability();
			
			return canAttach(capability);
		}
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
					currentOptions.remove(index);
					pipelineTable.setInput(current);
					pipelineTable.getTable().select(index == current.size() ? current.size()-1 : index);
					
					refreshPreview();
					
					break;
				}
				
				refreshMessage();
				capabilityTree.refresh();
			}
		}
	}
	
	private void refreshPreview(){
		if (previewWidget != null){
			if (current.size() > 0 && typeErrors().isEmpty()){
				ICapability pipe = null;
				try{
					 pipe = createPipeline();	
				}catch (Exception ex){
					// leave the old stuff up?
				}
				
				if (pipe != null){
					previewWidget.init(pipe, previewInput);
				}
			}else{
				previewWidget.init(new Exceptional(), previewInput);
			}
		}
	}
	
	private void refreshMessage(){
		this.setMessage(null);
		List<String> errors = typeErrors();
		if (current.isEmpty()){
			this.setMessage(DEFAULT_MESSAGE);
			this.setPageComplete(false);
		}else if (errors.isEmpty()){
			this.setErrorMessage(null);
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
	
	
	private class TableLabelProvider extends BaseLabelProvider implements ITableLabelProvider{
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
				if (CapabilityUtil.isUnary(capability)){
					return TypeManager.simpleTypeName(CapabilityUtil.unaryParameter(capability).getType().getType());
				} else {
					return null;
				}
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
		
		for (int i = 1; i < current.size(); i++){

			ICapability next = current.get(i);
			ICapability prior = null;

			try{
				prior = createPipeline(current.subList(0, i));
			}catch(Exception ex){
				continue; // sub-pipeline capability is invalid
			}

			TypeToken<?> priorOutput = CapabilityUtil.singleOutput(prior).getType();

			if (CapabilityUtil.isGenerator(next)
				|| TypeManager.isCompatible(CapabilityUtil.unaryParameter(next), priorOutput)
				|| isListCompatible(next, priorOutput))
			{
				continue;
			}else{
				ICapability.IParameter<?> nextParameter = CapabilityUtil.unaryParameter(next);
				
				result.add("Capability " + next.getName() + " with input " + TypeManager.simpleTypeName(nextParameter.getType()) + " is not compatible with type " + TypeManager.simpleTypeName(priorOutput.getType()));
			}	
		}
		return result;
	}

	
	private boolean isListCompatible(ICapability capability, TypeToken<?> input){
		if (List.class.isAssignableFrom(input.getRawType()) &&
			input.getType() instanceof ParameterizedType){
			
			Type elementType = ((ParameterizedType) input.getType()).getActualTypeArguments()[0];
			if (elementType instanceof Class){
				return TypeManager.isCompatible(CapabilityUtil.unaryParameter(capability), TypeToken.of(elementType));
			}
		}
		return false;
	}
	
	private class CapabilityTreeLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider{		
		@Override
		public Color getForeground(Object element, int columnIndex) {

			ICapability capability = element instanceof ICapability 
					?  (ICapability) element
					:  ((DerivedCapability) element).getCapability();

			return canAttach(capability) ? null : Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);

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

		private String getName(Object element){
			if (element instanceof ICapability){
				ICapability capability = (ICapability) element;
				return capability.getName();
			}else if (element instanceof DerivedCapability){
				DerivedCapability capability = (DerivedCapability) element;
				
				return capability.getGetter() == null ? 
						capability.getOutput().getName() :
						capability.getGetter().getName();
			}else{
				throw new RuntimeException("Unexpected tree element of type " + element.getClass());
			}
		}
		
		private String getDescription(Object element){
			if (element instanceof ICapability){
				return ((ICapability) element).getDescription();
			}else{
				return null;
			}
		}
		
		private String getInputType(Object element){
			if (element instanceof ICapability){
				return TypeManager.simpleTypeName(CapabilityUtil.unaryParameter((ICapability) element).getType());
			}else{
				return null;
			}
		}
		
		private String getOutputType(Object element){
			if (element instanceof ICapability){
				ICapability capability = (ICapability) element;
				return CapabilityUtil.hasSingleOutput(capability) ?
					   TypeManager.simpleTypeName(CapabilityUtil.singleOutput(capability).getType()) :
					   null;
			}else if (element instanceof DerivedCapability){
				DerivedCapability capability = (DerivedCapability) element;
				
				if (capability.getGetter() != null){
					return TypeManager.simpleTypeName(capability.getGetter().getOutput().getType());
				}else{
					return TypeManager.simpleTypeName(capability.getOutput().getType());
				}
				
			}else{
				return null;
			}
		}
		
		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex){
			case 0: return getName(element);
			case 1: return getInputType(element);
			case 2: return getOutputType(element);
			case 3: return getDescription(element);
			default: return null;
			}
		}
	}
	
	public static Object[] getDerived(ICapability c){
		if (CapabilityUtil.hasSingleOutput(c)){
			List<DerivedCapability> xs = DerivedCapability.derived(c, CapabilityUtil.singleOutput(c));
			Collections.sort(xs, new Comparator<DerivedCapability>(){
				@Override
				public int compare(DerivedCapability lhs, DerivedCapability rhs) {
					return lhs.getGetter().getName().compareTo(rhs.getGetter().getName());
				}
			});
			return xs.toArray();		
		}else{
			List<DerivedCapability> xs = DerivedCapability.derived(c);
			Collections.sort(xs, new Comparator<DerivedCapability>(){
				@Override
				public int compare(DerivedCapability lhs, DerivedCapability rhs) {
					return lhs.getOutput().getName().compareTo(lhs.getOutput().getName());
				}
			});
			return xs.toArray();
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
				ICapability c = (ICapability) parentElement;
				return getDerived(c);
			}else if (parentElement instanceof DerivedCapability){
				DerivedCapability c = (DerivedCapability) parentElement;
				
				if (c.getGetter() == null){
					List<DerivedCapability> xs = DerivedCapability.derived(c.getCapability(), c.getOutput());
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
			}else{
				return new Object[]{};
			}
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof DerivedCapability){
				DerivedCapability c = (DerivedCapability) element;
				
				if (c.getGetter() == null || CapabilityUtil.hasSingleOutput(c.getCapability())){
					return c.getCapability();
				}else{
					// TODO: track parent for getters of capabilities with multiple outputs
					return null;
				}
			}else{
				return null;
			}
		}

		@Override
		public boolean hasChildren(Object element) {
			// TODO make efficient
			return getChildren(element).length != 0;
		}
	}
	
	private void createPreviewGroup(Composite container){
		if (previewInput != null && previewInput.length > 0){
			previewGroup = new Group(container, SWT.SHADOW_ETCHED_IN);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			previewGroup.setText("Pipeline Results Preview");
			previewGroup.setLayoutData(data);
			
			previewGroup.setLayout(new GridLayout());
			
			previewWidget = new ReportWidget(previewGroup, SWT.NONE);
			previewWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			previewWidget.init(new Identity(), previewInput);
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
		return createPipeline(current);
	}
	
	private DynamicSerializablePipeline createPipeline(List<ICapability> capabilities){
		List<Serializable> descriptors = Lists.newArrayList();
		for (Object x : capabilities){
			if (x instanceof ICapability && CapabilityUtil.isSerializable((ICapability) x)){
				descriptors.add((Serializable) x);
			}else if (!(x instanceof ICapability) && x instanceof Serializable){
				descriptors.add((Serializable) x);
			}else{
				descriptors.add(((ICapability)x).getName());
			}
		}
		
		DynamicSerializablePipeline pipeline = new DynamicSerializablePipeline(
				nameEntry.getText(),
				descriptionEntry.getText(),
				descriptors,
				currentOptions);
		
		return pipeline;
	}
	
	private void buildCapabilityTree(Composite container){
		capabilityTree = new TreeViewer(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		capabilityTree.setContentProvider(new TreeContentProvider());
		capabilityTree.setLabelProvider(new CapabilityTreeLabelProvider());
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.minimumHeight = 325;
		if (previewInput == null || previewInput.length == 0){
			data.verticalSpan = 2;
		}
		capabilityTree.getTree().setLayoutData(data);
		
		capabilityTree.getTree().setHeaderVisible(true);
		
		TreeColumn nameColumn = new TreeColumn(capabilityTree.getTree(), SWT.LEFT);
		nameColumn.setWidth(225);
		nameColumn.setText("Capability");
		
		TreeColumn inputTypeColumn = new TreeColumn(capabilityTree.getTree(), SWT.LEFT);
		inputTypeColumn.setText("Input");
		inputTypeColumn.setWidth(100);
		
		TreeColumn outputTypeColumn = new TreeColumn(capabilityTree.getTree(), SWT.LEFT);
		outputTypeColumn.setText("Output");
		outputTypeColumn.setWidth(100);
		
		TreeColumn descriptionColumn = new TreeColumn(capabilityTree.getTree(), SWT.LEFT);
		descriptionColumn.setText("Description");
		descriptionColumn.setWidth(300);
		
		SortedSet<ICapability> unary = CupidPlatform.getCapabilityRegistry().getCapabilities(new Predicate<ICapability>(){
			@Override
			public boolean apply(ICapability capability) {
				return CapabilityUtil.isUnary(capability);
			}
		});
		
		capabilityTree.setInput(Lists.newArrayList(unary));
	}
	
	private void buildPipelineTable(Composite composite){
		pipelineTable = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.minimumWidth = 300;
		pipelineTable.getTable().setLayoutData(data);
		
		pipelineTable.setContentProvider(new TableContentProvider());
		pipelineTable.setLabelProvider(new TableLabelProvider());
		
		TableColumn nameColumn = new TableColumn(pipelineTable.getTable(), SWT.LEFT);
		nameColumn.setText("Capability");
		
		TableColumn inputTypeColumn = new TableColumn(pipelineTable.getTable(), SWT.LEFT);
		inputTypeColumn.setText("Input Type");
		
		TableColumn outputTypeColumn = new TableColumn(pipelineTable.getTable(), SWT.LEFT);
		outputTypeColumn.setText("Output Type");
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(2));
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(1));
		pipelineTable.getTable().setLayout(layout);
		
		pipelineTable.getTable().setHeaderVisible(true);
	}
	
	private void clearOptions(){
        for (Widget control : optionWidgets){
        	control.dispose();
        }
        
        newOptions = new CapabilityArguments();
        
        optionWidgets.clear();
        optionInputs.clear();
	}	

	
	private void buildOptionEditor(Composite composite){
		optionGroup = new Group(composite, SWT.BORDER);
		optionGroup.setText("Capability Options");
		
		GridData dGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		dGroup.minimumHeight = 200;
		optionGroup.setLayoutData(dGroup);
		
		optionGroup.setLayout(new GridLayout());
		
		optionContainer = new ScrolledComposite(optionGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		optionContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		optionContainer.setLayout(new FillLayout());	
		 
        optionInnerContainer = new Composite(optionContainer, SWT.NONE);
		optionInnerContainer.setLayout(new GridLayout());
		optionContainer.setContent(optionInnerContainer);
        
		optionContainer.setExpandVertical(true);
		optionContainer.setExpandHorizontal(true);
	}

	private class CapabilityOutputPair{
		private ICapability capability;
		private ICapability.IOutput<?> output;
		
		private CapabilityOutputPair(ICapability capability, IOutput<?> output) {
			super();
			this.capability = capability;
			this.output = output;
		}
	}
	
	private List<CapabilityOutputPair> capabilitiesForOption(TypeToken<?> pipeInput, ICapability.IParameter<?> option){
		
		List<CapabilityOutputPair> result = Lists.newArrayList();
		
		for (ICapability capability : CupidPlatform.getCapabilityRegistry().getCapabilities(pipeInput, option.getType())){
			for (ICapability.IOutput<?> output : capability.getOutputs()){
				if (TypeManager.isJavaCompatible(option.getType(), output.getType())){
					result.add(new CapabilityOutputPair(capability, output));
				}
			}
		}
		
		return result;
	}
	
	private void showOptions(final ICapability capability, final Integer pipeIndex){
        if (!optionWidgets.isEmpty()){
        	clearOptions();
        }
          
        if (capability == null){
        	return;
        }
        
       
    	final CapabilityArguments capabilityOptions = (pipeIndex == null)
    			? newOptions
    			: currentOptions.get(pipeIndex);
        
        for (final ICapability.IParameter<?> option : CapabilityUtil.options(capability)){
        	
        	@SuppressWarnings("rawtypes")
			final OptionEditor input = OptionEditorFactory.getEditor(capability, option);
  
        	if (input == null){
        		continue;
        	}
        	
        	boolean hasValue = capabilityOptions.hasValueArgument(option);
        	
        	Label label = new Label(optionInnerContainer, SWT.LEFT);
        	label.setText(option.getName());
        	
        	Composite cOption = new Composite(optionInnerContainer, SWT.NONE);
        	cOption.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        	GridLayout lOption = new GridLayout() ;
        	lOption.numColumns = 2;
        	cOption.setLayout(lOption);
        	
        	GridData dLabel = new GridData();
        	dLabel.verticalSpan = 2;
        	dLabel.verticalAlignment = SWT.TOP;
        	label.setLayoutData(dLabel);
        
        	Button bConstant = new Button(cOption, SWT.RADIO);
        	bConstant.setText("Constant:");
        	
        	final Control entry = hasValue ?
        			input.create(cOption, capabilityOptions.getValueArgument(option)) :
        			input.create(cOption, option.getDefault());
        
        	bConstant.setSelection(hasValue);
            entry.setEnabled(hasValue);    	
        			
        	Button bCapability = new Button(cOption, SWT.RADIO);
        	bCapability.setText("Capability:");
        	
        	final Combo cCombo = new Combo(cOption, SWT.READ_ONLY);
        	cCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        	
        	bCapability.setSelection(!hasValue);
        	cCombo.setEnabled(!hasValue);
        	
        	TypeToken<?> pipeInput = null;
        	
        	if (pipeIndex == null){
        		pipeInput = CapabilityUtil.isGenerator(capability) 
        				? TypeToken.of(Void.class)
        				: CapabilityUtil.unaryParameter(capability).getType();
        	}else{
        		ICapability first = current.get(0);
        		pipeInput = CapabilityUtil.isGenerator(first) 
        				? TypeToken.of(Void.class)
        				: CapabilityUtil.unaryParameter(first).getType();
        	}
        	
        	final List<CapabilityOutputPair> compatible = capabilitiesForOption(pipeInput, option);
        	
        	if (!compatible.isEmpty()){
        		
            	for (CapabilityOutputPair c : compatible){
            		if (CapabilityUtil.hasSingleOutput(c.capability)){
            			cCombo.add(c.capability.getName());		
            		}else{
            			cCombo.add(c.capability.getName() + " - " + c.output.getName());
            		}
            	}
        	}else{
        		bCapability.setEnabled(false);
        	}
        	    
    		if (!hasValue){
    			OutputSelector s = (OutputSelector) capabilityOptions.getCapabilityArgument(option);
    			try {
					ICapability c = s.getCapability();
					if (CapabilityUtil.hasSingleOutput(c)){
						cCombo.setText(c.getName());
					}else{
						cCombo.setText(c.getName() + " - " + s.getOutput().getName());
					}
				} catch (NoSuchCapabilityException e) {
					throw new RuntimeException(e);
				}	
    		}
        	
        	input.addValueChangedListener(new ValueChangedListener(){
				@Override
				public void valueChanged(OptionEditor option, Object value) {
					capabilityOptions.add((IParameter)input.getOption(), value);
				}
        	});
        	
        	cCombo.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					CapabilityOutputPair o = compatible.get(cCombo.getSelectionIndex());
					capabilityOptions.add(option, new OutputSelector(o.capability, o.output));
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// NO OP
				}
			});
        	
        	bConstant.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					cCombo.setEnabled(false);
					entry.setEnabled(true);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// NO OP
				}
			});
        	
        	bCapability.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					cCombo.setEnabled(true);
					entry.setEnabled(false);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// NO OP
				}
			});
        	
        	
        	optionContainer.setMinSize(optionInnerContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        	
        	optionInputs.put(option, input);
        	optionWidgets.add(cOption);
        	optionWidgets.add(input.getWidget());
        	optionWidgets.add(label);
        }

        optionGroup.layout(true);
        optionInnerContainer.setSize(optionInnerContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
}
