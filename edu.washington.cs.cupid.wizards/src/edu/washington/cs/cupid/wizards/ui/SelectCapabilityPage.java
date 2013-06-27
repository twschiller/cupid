package edu.washington.cs.cupid.wizards.ui;

import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.ILinearCapability;
import edu.washington.cs.cupid.scripting.java.SnippetSourceView;
import edu.washington.cs.cupid.scripting.java.SnippetSourceView.ModifyListener;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.TypeSelection;

public class SelectCapabilityPage extends WizardPage {

	public interface SelectListener{
		public void onSelect(ILinearCapability<?,?> capability);
	}
	
	private TypeToken<?> inputType;
	private final TypeToken<?> outputType;
	
	private Composite container = null;
	private SnippetSourceView fViewer;
	private Class<?> startType;
	private final List<SelectListener> listeners = Lists.newArrayList();
	
	protected SelectCapabilityPage(Class<?> startType,  TypeToken<?> outputType) {
		super("Select");
		this.setTitle("Define formatting predicate for " + startType.getSimpleName());
		this.setMessage("Define a predicate for the formatting rule");
		this.setPageComplete(false);
		this.startType = startType;
		this.inputType = TypeToken.of(startType);
		this.outputType = outputType;
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
		selectType.select(0);
		
		Label selectCapabilityLbl = new Label(container, SWT.LEFT);
		selectCapabilityLbl.setText("Select Capability (Optional):");
		selectCapabilityLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		Combo matching = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY); 
		matching.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label writeRuleLbl = new Label(container, SWT.LEFT);
		writeRuleLbl.setText("Formatting condition expression:");
		GridData lblData = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
		lblData.horizontalSpan = 2;
		writeRuleLbl.setLayoutData(lblData);
		
		fViewer = new SnippetSourceView(container, SWT.NONE);
		fViewer.setSnippetType(TypeToken.of(startType), outputType);
		
		enableContentAssist();
		
		GridData d = new GridData(SWT.FILL, SWT.FILL, true, true);
		d.horizontalSpan = 2;
		fViewer.setLayoutData(d);
		
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
			if (msgs.getDiagnostics().isEmpty()){
				setPageComplete(true);
				setErrorMessage(null);
				setMessage("Expression is valid", WizardPage.INFORMATION);
			}else{
				setPageComplete(false);
				setMessage(null);
				setErrorMessage(cleanMsg(msgs.getDiagnostics().get(0).getMessage(null)));
			}
		}
	}
	
	private void setInputType(String qualifiedName){
		try {
			inputType = TypeToken.of(Class.forName(qualifiedName));
			setTitle("Define formatting predicate for " + inputType.getRawType().getSimpleName());
			fViewer.setSnippetType(inputType, outputType);
			enableContentAssist();
		} catch (ClassNotFoundException ex) {
			Activator.getDefault().logError("Error loading type " + qualifiedName, ex);
			setErrorMessage("Error loading type " + qualifiedName);
			setPageComplete(false);
		}
	}

	private static String cleanMsg(String msg){
		return msg;
	}
	
	public void performCleanup() {
		fViewer.performCleanup();
	}
}
