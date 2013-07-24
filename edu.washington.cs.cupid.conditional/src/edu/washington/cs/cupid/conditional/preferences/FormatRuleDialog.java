package edu.washington.cs.cupid.conditional.preferences;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.internal.Activator;
import edu.washington.cs.cupid.conditional.ui.ColorPickerButton;
import edu.washington.cs.cupid.conditional.ui.FontPickerButton;
import edu.washington.cs.cupid.scripting.java.SnippetSourceView;

/**
 * Dialog for editing existing (or new) formatting rules
 * @author Todd Schiller
 */
public class FormatRuleDialog extends TitleAreaDialog {

	private static final String CONTENT_ASSIST_ERR_MSG = "Error enabling content assist for snippet";
	private static final String INPUT_TYPE_ERR_MSG = "Error loading input type for formatting rule";
	
	private FormattingRule rule = null;
	
	private TypeToken<?> inputType = null;
	
	private TabFolder folder;
	private Composite cFormat;
	private Composite cPredicate;
	
	private SnippetSourceView vSnippet;
	private Combo cCapability;
	private Combo cCapabilityOutput;
	
	private String errorMsg = null;
	private String snippetErrorMsg = null;
	
	/**
	 * List of currently available predicates for the specified input type.
	 */
    private List<ICapability> compatible;
    
	/**
     * True iff the user hasn't selected a capability yet for {@link active}, the selected item.
     */
    private boolean containsSentinalEntry = false;
	
	public FormatRuleDialog(FormattingRule rule, Shell parentShell) {
		super(parentShell);
		this.rule = rule.copy();
	}

	@Override
	public void create() {
	
		if (rule.getQualifiedType() != null){
			try {
				inputType = TypeManager.forName(rule.getQualifiedType());
				updateCompatible();
			} catch (ClassNotFoundException e) {
				compatible = Lists.newArrayList();
				Activator.getDefault().logError("Error loading input type for formatting rule; type: " + rule.getQualifiedType(), e);
			}		
		}else{
			inputType = null;
		}
		
		super.create();
		super.setTitle("Edit Formatting Rule: " + rule.getName());
		super.setMessage("Specify the formatting condition and the formatting options");
	}

	private void updateCompatible(){
		compatible = Lists.newArrayList();
		for (ICapability c : CupidPlatform.getCapabilityRegistry().getCapabilities(inputType)){
			if (!CapabilityUtil.isGenerator(c) && CapabilityUtil.isLinear(c)){
				compatible.add(c);
			}
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label lName = new Label(container, SWT.LEFT);
		lName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lName.setText("Name:");
		
		final Text tName = new Text(container, SWT.SINGLE | SWT.BORDER);
		GridData dName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		dName.horizontalSpan = 2;
		tName.setLayoutData(dName);
		
		tName.setText(rule.getName());
		tName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				rule.setName(tName.getText());
				checkModel();
			}
		});
		
		Label lType = new Label(container, SWT.LEFT);
		lType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lType.setText("Type:");
		final Text tType = new Text(container, SWT.SINGLE | SWT.BORDER);
		tType.setText(rule.getQualifiedType() == null ? "" : rule.getQualifiedType());
		tType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		tType.setEditable(false);
		tType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onSelectType(tType.getText());
			}
		});
		Button bType = new Button(container, SWT.PUSH);
		bType.setText("Select");
		bType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object [] objs = showTypeDialog();
				if (objs != null) {
					IType type = (IType) objs[0];
					tType.setText(type.getFullyQualifiedName());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		folder = new TabFolder(container, SWT.TOP);
		GridData dFolder = new GridData(SWT.FILL, SWT.FILL, true, true);
		dFolder.horizontalSpan = 3;
		folder.setLayoutData(dFolder);
		
		cFormat = new Composite(folder, SWT.NONE);
		cPredicate = new Composite(folder, SWT.NONE);
	
		TabItem tPredicate = new TabItem(folder, SWT.NONE);
		tPredicate.setControl(cPredicate);
		tPredicate.setText("Condition");
		buildPredicateEditor();
		
		TabItem tFormat = new TabItem(folder, SWT.NONE);
		tFormat.setControl(cFormat);
		tFormat.setText("Formatting");
		buildFormatEditor();
		
		container.pack(true);
		
		return parent;
	}
	
	private void setType(String qualifiedName) throws ClassNotFoundException{
		inputType = null;
		this.inputType = TypeManager.forName(qualifiedName);
	}
	
	private void onSelectType(String qualifiedName){

		rule.setQualifiedType(qualifiedName);
		try {
			setType(qualifiedName);
		} catch (ClassNotFoundException e) {
			ErrorDialog.openError(getShell(), 
					"Error loading bundle for input type",
					"Error loading formatting rule input type: " + qualifiedName + "; see log for more details.",
					new Status(Status.WARNING, Activator.PLUGIN_ID, Status.OK, INPUT_TYPE_ERR_MSG, e));
					
			Activator.getDefault().logError("Error loading input type for formatting rule; type: " + qualifiedName, e);
		}
		
		if (this.inputType == null) return;
		
		vSnippet.setSnippetType(this.inputType, TypeToken.of(boolean.class));
		
		try{
			vSnippet.enableContentAssist();
		}catch (Exception e){
			ErrorDialog.openError(getShell(), 
					"Error enabling content assist",
					CONTENT_ASSIST_ERR_MSG,
					new Status(Status.WARNING, Activator.PLUGIN_ID, Status.OK, CONTENT_ASSIST_ERR_MSG, e));
		}
		
		updateCompatible();
		updateCapabilityList();
		checkModel();
	}
	
	private void buildFormatEditor(){
		
		GridLayout lFormat = new GridLayout();
		lFormat.numColumns = 4;
		cFormat.setLayout(lFormat);
		
		Label lBackground = new Label(cFormat, SWT.LEFT);
		lBackground.setText("Background Color:");
		final ColorPickerButton bBackground = new ColorPickerButton(cFormat, rule.getFormat().getBackground());
		bBackground.setLayoutData(new GridData(60, 50));
		bBackground.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				rule.getFormat().setBackground(bBackground.getColor());
			}
		});
		
		Label lForeground = new Label(cFormat, SWT.LEFT);
		lForeground.setText("Foreground Color:");
		final ColorPickerButton bForeground = new ColorPickerButton(cFormat, rule.getFormat().getForeground());	
		bForeground.setLayoutData(new GridData(60, 50));
		bForeground.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				rule.getFormat().setForeground(bForeground.getColor());
			}
		});
		
		Label lFont = new Label(cFormat, SWT.LEFT);
		lFont.setText("Font:");
		final FontPickerButton bFont = new FontPickerButton(cFormat, rule.getFormat().getFont());
		bFont.setText("Select");
		bFont.setLayoutData(new GridData(70, 50));
		
		bFont.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				rule.getFormat().setFont(bFont.getFontData());
			}
		});
	}
	
	private void buildPredicateEditor(){
		GridLayout lPredicate = new GridLayout();
		lPredicate.numColumns = 2;
		cPredicate.setLayout(lPredicate);
		
		Label lCapability = new Label(cPredicate, SWT.LEFT);
		lCapability.setText("Capability (Optional):");
		
		cCapability = new Combo(cPredicate, SWT.READ_ONLY | SWT.DROP_DOWN);
		cCapability.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label lCapabilityOutput = new Label(cPredicate, SWT.LEFT);
		lCapabilityOutput.setText("Capability Output:");
	
		cCapabilityOutput = new Combo(cPredicate, SWT.READ_ONLY | SWT.DROP_DOWN);
		cCapabilityOutput.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		updateCapabilityList();
		
		cCapability.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (!compatible.isEmpty()) {
					if (containsSentinalEntry) {
						if (cCapability.getSelectionIndex() > 1) {
							setCapability(compatible.get(cCapability.getSelectionIndex() - 2));
							cCapability.remove(1);
							containsSentinalEntry = false;
						}else if (cCapability.getSelectionIndex() == 0){
							cCapability.remove(1);
							containsSentinalEntry = false;
							setCapability(null);
						}
					} else {
						if (cCapability.getSelectionIndex() > 0){
							setCapability(compatible.get(cCapability.getSelectionIndex() - 1));			
						}else{
							setCapability(null);
						}
					}
				}
			}
		});
		
		cCapabilityOutput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setCapabilityOutput(cCapabilityOutput.getText());
			}
		});
		
		vSnippet = new SnippetSourceView(cPredicate, SWT.NONE);
		
		GridData gSnippet = new GridData(SWT.FILL, SWT.FILL, true, true);
		gSnippet.horizontalSpan = 2;
		vSnippet.setLayoutData(gSnippet);
		
		TypeToken<?> snippetInput = null;
		
		if (rule.getCapabilityId() == null){
			if (rule.getQualifiedType() != null){
				try {
					snippetInput = TypeManager.forName(rule.getQualifiedType());
				} catch (ClassNotFoundException e) {
					Activator.getDefault().logError("Error loading formatting rule input type: " + rule.getQualifiedType(), e);
				}
			}
		}else{
			try {
				ICapability c = Activator.findRuleCapability(rule);
				snippetInput = CapabilityUtil.findOutput(c, rule.getCapabilityOutput()).getType();
			} catch (Exception e) {
				// capability does not exist
			}
		}
	
		if (snippetInput != null){
			setSnippetInput(snippetInput);	
		}

		vSnippet.setContent(rule.getSnippet());	
		vSnippet.addModifyListener(new SnippetHandler());
	}
	
	private void setSnippetInput(TypeToken<?> snippetInput){
		vSnippet.setSnippetType(snippetInput, TypeToken.of(boolean.class));
		
		try {
			vSnippet.enableContentAssist();
		} catch (Exception ex) {
			Activator.getDefault().logError("Error loading content assist for formatting rule type: " + snippetInput.toString(), ex);
		}
	}
	
	private void setCapability(ICapability capability){
		if (capability == null){
			rule.setCapabilityId(null);
			setSnippetInput(inputType);	
			setCapabilityOutput(null);
			cCapabilityOutput.removeAll();
			cCapabilityOutput.setEnabled(false);
		}else{
			rule.setCapabilityId(capability.getName());
			
			cCapabilityOutput.removeAll();
			
			for (ICapability.IOutput<?> o : capability.getOutputs()){
				cCapabilityOutput.add(o.getName());
			}
			
			cCapabilityOutput.select(0);
			cCapabilityOutput.setEnabled(capability.getOutputs().size() > 1);
		}
		checkModel();			
	}
	
	private void setCapabilityOutput(String outputName){
		if (outputName != null){
			ICapability c;
			try {
				c = Activator.findRuleCapability(rule);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			ICapability.IOutput<?> o = CapabilityUtil.findOutput(c, outputName);
			rule.setCapabilityOutput(outputName);
			setSnippetInput(o.getType());
		}else{
			rule.setCapabilityOutput(null);
		}
	}
	
	private class SnippetHandler implements SnippetSourceView.ModifyListener{
		@Override
		public void onModify(String snippet,
				DiagnosticCollector<JavaFileObject> msgs) {
		
			if (!msgs.getDiagnostics().isEmpty()){
				snippetErrorMsg = msgs.getDiagnostics().get(0).getMessage(null);
			}else{
				snippetErrorMsg = null;
			}
			
			checkModel();
		}
	}
	
	/**
	 * Update the list of compatible capabilities
	 */
	private void updateCapabilityList(){
		
		cCapability.removeAll();
		
		if (compatible == null || compatible.isEmpty()) {
			addAndSet(cCapability, "-- No Available Capabilities --");
		} else {
			ICapability forRule = null;

			addAndSet(cCapability, "-- No Transformer Capability --");
			
			if (rule.getCapabilityId() != null) {
				try {
					forRule = Activator.findRuleCapability(rule);
					containsSentinalEntry = false;
				} catch (Exception e) {
					addAndSet(cCapability, "-- Unknown Capability --");
					containsSentinalEntry = true;
				}
			} 
			
			for (ICapability capability : compatible) {
				if (forRule == capability) {
					addAndSet(cCapability, capability.getName());
					
					cCapabilityOutput.removeAll();
					for (ICapability.IOutput<?> o : capability.getOutputs()){
						cCapabilityOutput.add(o.getName());
					}
					
					if (rule.getCapabilityOutput() != null){
						cCapabilityOutput.setText(rule.getCapabilityOutput());
					}else{
						cCapabilityOutput.select(0);
					}
					
				} else {
					cCapability.add(capability.getName());
				}
			}
		}
	}

	private void checkModel(){
		errorMsg = null;
		this.setErrorMessage(null);
		this.setMessage(null);
			
		if (rule.getName().trim().isEmpty()){
			errorMsg = "Formatting rule must have non-empty name";
		}else if (rule.getQualifiedType() == null){
			errorMsg = "Formatting rule must specify type to format";
		}else if (snippetErrorMsg != null){
			errorMsg = snippetErrorMsg;
		}
		
		this.setErrorMessage(errorMsg);
		
		if (errorMsg == null){
			this.setMessage("Formatting rule is valid");
		}
	}
	
	public FormattingRule getRule(){
		return rule;
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
	
	private Object[] showTypeDialog() {
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
}
