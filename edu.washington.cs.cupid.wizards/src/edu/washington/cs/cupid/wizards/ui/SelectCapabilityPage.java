package edu.washington.cs.cupid.wizards.ui;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.scripting.java.SnippetSourceView;
import edu.washington.cs.cupid.scripting.java.SnippetSourceView.ModifyListener;
import edu.washington.cs.cupid.types.ITypeAdapter;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.TypeSelection;

public class SelectCapabilityPage extends WizardPage {

	public interface SelectListener{
		public void onSelectType(TypeToken<?> type);
	}
	
	private TypeToken<?> inputType = null;
	private ICapability capability = null;
	private ICapability.IOutput<?> capabilityOutput = null;
	
	private final TypeToken<?> outputType;
	
	private Combo cCapability; 
	private Combo cCapabilityOutput;
	
	private Composite container = null;
	private SnippetSourceView fViewer;
	private Class<?> startType;
	private final List<SelectListener> listeners = Lists.newArrayList();
	
	private boolean suppressEvents = true;
	
	/**
	 * List of currently available predicates for the specified input type.
	 */
    private List<ICapability> compatible;
	
	protected SelectCapabilityPage(Class<?> startType,  TypeToken<?> outputType) {
		super("Select");	
		this.setTitle("Define formatting predicate for " + startType.getSimpleName());
		this.setMessage("Define a predicate for the formatting rule");
		this.setPageComplete(false);
		this.startType = startType;
		this.inputType = TypeToken.of(startType);
		this.outputType = outputType;
		this.updateCompatible();
	}
	
	public void addSelectListener(SelectListener listener){
		listeners.add(listener);
	}
	
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		Label selectInputTypeLbl = new Label(container, SWT.LEFT);
		selectInputTypeLbl.setText("Select Input Type: ");
		selectInputTypeLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		final Combo selectType = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY); 
		selectType.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		for (Class<?> c : TypeSelection.getSuperTypes(TypeToken.of(startType))){
			selectType.add(c.getName());
		}
		
		// TODO: this should probably be done when determining the common type of all the selections
		for (ITypeAdapter<?, ?> adapter : TypeManager.getTypeAdapterRegistry().getTypeAdapters(TypeToken.of(startType))){
			selectType.add(adapter.getOutputType().getRawType().getName());
		}
		
		selectType.select(0);
		
		Label selectCapabilityLbl = new Label(container, SWT.LEFT);
		selectCapabilityLbl.setText("Select Capability (Optional):");
		selectCapabilityLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		cCapability = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY); 
		cCapability.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label lCapabilityOutput = new Label(container, SWT.LEFT);
		lCapabilityOutput.setText("Capability Output:");
		lCapabilityOutput.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
	
		cCapabilityOutput = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		cCapabilityOutput.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		cCapability.addModifyListener(new org.eclipse.swt.events.ModifyListener() {	
			@Override
			public void modifyText(ModifyEvent e) {
				if (!compatible.isEmpty()) {
					if (cCapability.getSelectionIndex() > 0){
						setCapability(compatible.get(cCapability.getSelectionIndex() - 1));			
					}else{
						setCapability(null);
					}
				}
			}
		});
		
		cCapabilityOutput.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setCapabilityOutput(cCapabilityOutput.getText());
			}
		});
		
		Label writeRuleLbl = new Label(container, SWT.LEFT);
		writeRuleLbl.setText("Formatting condition expression:");
		GridData lblData = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
		lblData.horizontalSpan = 2;
		writeRuleLbl.setLayoutData(lblData);
		
		fViewer = new SnippetSourceView(container, SWT.NONE);
		fViewer.setSnippetType(TypeToken.of(startType), outputType);
		
		enableContentAssist();
		
		GridData dViewer = new GridData(SWT.FILL, SWT.FILL, true, true);
		dViewer.horizontalSpan = 2;
		fViewer.setLayoutData(dViewer);
		
		fViewer.addModifyListener(new SnippetListener());
		
		this.setControl(container);
		
		selectType.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				setInputType(selectType.getText());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		updateCapabilityList();
		suppressEvents = false;
	}
	
	private void enableContentAssist(){
		try {
			fViewer.enableContentAssist();
		} catch (Exception e) {
			this.setErrorMessage("Error enabling content assist for snippet editor; see Eclipse log for more information");
			Activator.getDefault().logError("Error enabling content assist for snippet editor", e);
		}
	}
	
	private class SnippetListener implements ModifyListener{
		@Override
		public void onModify(String snippet, DiagnosticCollector<JavaFileObject> msgs) {
			List<Diagnostic<?>> errors = Lists.newArrayList();
			for (Diagnostic<?> d : msgs.getDiagnostics()){
				if (d.getKind() == Diagnostic.Kind.ERROR){
					errors.add(d);
				}
			}
			
			if (errors.isEmpty()){
				setPageComplete(true);
				setErrorMessage(null);
				setMessage("Expression is valid", WizardPage.INFORMATION);
			}else{
				setPageComplete(false);
				setMessage(null);
				setErrorMessage(cleanMsg(errors.get(0).getMessage(null)));
			}
		}
	}
	
	/**
	 * Add an entry to the combo box, and select it.
	 * @param combo the combo box
	 * @param text the text to add and select
	 */
	private static void addAndSet(final Combo combo, final String text) {
		combo.add(text);
		combo.setText(text);
	}
	
	private void updateCapabilityList(){
		cCapability.removeAll();
		
		if (compatible == null || compatible.isEmpty()) {
			cCapability.add("-- No Available Capabilities --");
		} else {
			addAndSet(cCapability, "-- Select Transformer Capability --");
			
			for (ICapability c : compatible) {
				if (this.capability == c) {
					addAndSet(cCapability, c.getName());
				} else {
					cCapability.add(c.getName());
				}
			}
		}
	}
	
	private void updateCompatible(){
		compatible = Lists.newArrayList();
		for (ICapability c : CupidPlatform.getCapabilityRegistry().getCapabilities(inputType)){
			if (!CapabilityUtil.isGenerator(c) && CapabilityUtil.isUnary(c)){
				compatible.add(c);
			}
		}
	}
	
	private void setInputType(String qualifiedName){
		try {
			inputType = TypeToken.of(Class.forName(qualifiedName));
			setTitle("Define formatting predicate for " + inputType.getRawType().getSimpleName());
			updateCompatible();
			updateCapabilityList();
		} catch (ClassNotFoundException ex) {
			Activator.getDefault().logError("Error loading type " + qualifiedName, ex);
			setErrorMessage("Error loading type " + qualifiedName);
			setPageComplete(false);
		}
	}
	
	private void setCapability(ICapability capability){
		this.capability = capability;
		
		if (capability == null){
			setSnippetInputType(inputType);	
			
			capabilityOutput = null;
			cCapabilityOutput.removeAll();
			cCapabilityOutput.setEnabled(false);
		}else{
			cCapabilityOutput.removeAll();
			for (ICapability.IOutput<?> output : capability.getOutputs()){
				cCapabilityOutput.add(output.getName());
			}
			cCapabilityOutput.select(0);
			cCapabilityOutput.setEnabled(capability.getOutputs().size() > 1);
		}
	}
	
	private void setCapabilityOutput(String outputName){
		if (capability == null || outputName == null){
			setSnippetInputType(inputType);
		}else{
			capabilityOutput = CapabilityUtil.findOutput(capability, outputName);
			setSnippetInputType(capabilityOutput.getType()); // TODO: resolve type variables
		}
	}
	
	private void setSnippetInputType(TypeToken<?> type){
		fViewer.setSnippetType(type, outputType);
		
		if (!suppressEvents){
			for (SelectListener listener : listeners){
				listener.onSelectType(inputType);
			}			
		}
		
		enableContentAssist();
	}

	private static String cleanMsg(String msg){
		return msg;
	}
	
	public TypeToken<?> getInputType() {
		return inputType;
	}
	
	public String getSnippet(){
		return fViewer.getSnippet();
	}

	public void performCleanup() {
		fViewer.performCleanup();
	}
	
	public ICapability getCapability(){
		return capability;
	}

	public ICapability.IOutput<?> getCapabilityOutput() {
		return capabilityOutput;
	}
}
