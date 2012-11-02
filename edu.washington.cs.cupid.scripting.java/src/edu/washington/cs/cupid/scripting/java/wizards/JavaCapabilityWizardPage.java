package edu.washington.cs.cupid.scripting.java.wizards;

import java.lang.reflect.Type;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import edu.washington.cs.cupid.scripting.java.internal.Activator;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (cupid).
 */
public class JavaCapabilityWizardPage extends WizardPage {

	private Text nameText;
	private Text descriptionText;
	
	private Text parameterType;
	private Text outputType;

	private Text idText;
	
	/**
	 * Constructor for JavaCapabilityWizardPage.
	 * @param pageName
	 */
	public JavaCapabilityWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("New Cupid Capability");
		setDescription("Create a new Java Cupid Capability");
	}

	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		addLabel(container, "&Name:");
		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		initText(nameText, 2);
		
		addLabel(container, "&Description:");
		descriptionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		initText(descriptionText, 2);
		
		addLabel(container, "&Unique Id:");
		idText = new Text(container, SWT.BORDER | SWT.SINGLE);
		initText(idText, 2);
		
		addLabel(container, "&Parameter Type:");
		parameterType = new Text(container, SWT.BORDER | SWT.SINGLE);
		final Button inputSelect = new Button(container, SWT.PUSH);
		
		inputSelect.addMouseListener(new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseUp(MouseEvent e) {
				Object[] objs = showTypeDialog();
				
				if (objs != null){
					parameterType.setText(((IType)objs[0]).getFullyQualifiedName());
				}
			}
		});
		
		inputSelect.setText("Select");
		initText(parameterType, 1);
		
		addLabel(container, "&Output Type:");
		outputType = new Text(container, SWT.BORDER | SWT.SINGLE);
		final Button outputSelect = new Button(container, SWT.PUSH);
		outputSelect.setText("Select");
		initText(outputType, 1);
		
		outputSelect.addMouseListener(new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseUp(MouseEvent e) {
				Object[] objs = showTypeDialog();
				if (objs != null){
					outputType.setText(((IType)objs[0]).getFullyQualifiedName());
				}
			}
		});
		
		initialize();
		dialogChanged();
		setControl(container);
	}
	
	private Object[] showTypeDialog(){
		SelectionDialog dialog;
		try {
			dialog = JavaUI.createTypeDialog(this.getShell(), 
					null,
					Activator.getDefault().getCupidProject(),
					IJavaElementSearchConstants.CONSIDER_TYPES,
					false);
		} catch (JavaModelException e) {
			return null;
			// NO OP
		}
		dialog.open();

		return dialog.getResult();
	}

	private Label addLabel(Composite container, String text){
		Label label = new Label(container, SWT.NONE);
		label.setText(text);
		return label;
	}
	
	private void initText(Text text, int span){
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = span;
		text.setLayoutData(data);
		
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
	}
	
	private void initialize() {
		nameText.setText("MyCapability");
		descriptionText.setText("My Capability Description");
		idText.setText("edu.washington.cs.cupid.custom.mycapability");
		parameterType.setText("org.eclipse.core.resources.IResource");
		outputType.setText("org.eclipse.core.resources.IResource");
	}

	private void dialogChanged() {

		String name = getCapabilityName();
		String description = getCapabilityDescription();
		String id = getUniqueId();

		if (name.length() == 0) {
			updateStatus("Capability name must be specified");
			return;
		}
		
		if (id.length() == 0){
			updateStatus("Unique id must be specified");
			return;
		}
		
		if (description.length() == 0) {
			updateStatus("Capability description must be specified");
			return;
		}
		
		try {
			@SuppressWarnings("unused")
			Class<?> paramType = getParameterType();
		} catch (ClassNotFoundException e) {
			updateStatus("Unknown parameter type " + parameterType.getText());
			return;
		}
		
		try {
			@SuppressWarnings("unused")
			Class<?> outputType = getReturnType();
		} catch (ClassNotFoundException e) {
			updateStatus("Unknown return type " + outputType.getText());
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public Class<?> getParameterType() throws ClassNotFoundException {
		return Class.forName(parameterType.getText());
	}
	
	public Class<?> getReturnType() throws ClassNotFoundException {
		return Class.forName(outputType.getText());
	}
	
	public String getUniqueId(){
		return idText.getText();
	}
	
	public String getCapabilityName() {
		return nameText.getText();
	}
	
	public String getCapabilityDescription() {
		return descriptionText.getText();
	}
}