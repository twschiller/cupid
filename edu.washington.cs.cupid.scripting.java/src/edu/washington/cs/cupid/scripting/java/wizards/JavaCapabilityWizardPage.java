package edu.washington.cs.cupid.scripting.java.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * A wizard page for configuring a new Java capability script.
 * @author Todd Schiller
 */
public final class JavaCapabilityWizardPage extends WizardPage {

	private static final String DEFAULT_TYPE = "org.eclipse.core.resources.IResource";
	private Text nameText;
	private Text descriptionText;
	
	private Text parameterType;
	private Text outputType;

	private IPath parameterTypeReference;
	private IPath outputTypeReference;
	
	private Text idText;
	
	/**
	 * Construct a wizard page for configuring a new Java capability script.
	 * @param selection currently does nothing
	 */
	public JavaCapabilityWizardPage(final ISelection selection) {
		super("wizardPage");
		setTitle("New Cupid Capability");
		setDescription("Create a new Java Cupid Capability");
	}
	
	@Override
	public void createControl(final Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		addLabel(container, "&Name:");
		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		initText(nameText, 2);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				validateName();
			}
		});
		
		addLabel(container, "&Description:");
		descriptionText = new Text(container, SWT.BORDER | SWT.SINGLE);
		initText(descriptionText, 2);
		
		addLabel(container, "&Unique Id:");
		idText = new Text(container, SWT.BORDER | SWT.SINGLE);
		initText(idText, 2);
		
		addLabel(container, "&Parameter Type:");
		parameterType = new Text(container, SWT.BORDER | SWT.SINGLE);
		final Button inputSelect = new Button(container, SWT.PUSH);
		
		inputSelect.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseDown(final MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseUp(final MouseEvent e) {
				Object[] objs = showTypeDialog();
				
				if (objs != null) {
					IType type = (IType) objs[0];
					parameterTypeReference = type.getPath();
					parameterType.setText(type.getFullyQualifiedName());
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
		
		outputSelect.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseDown(final MouseEvent e) {
				// NO OP
			}

			@Override
			public void mouseUp(final MouseEvent e) {
				Object[] objs = showTypeDialog();
				if (objs != null) {
					IType type = (IType) objs[0];
					outputTypeReference = type.getPath();
					outputType.setText(type.getFullyQualifiedName());
				}
			}
		});
		
		initialize();
		dialogChanged();
		setControl(container);
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

	private Label addLabel(final Composite container, final String text) {
		Label label = new Label(container, SWT.NONE);
		label.setText(text);
		return label;
	}
	
	private void initText(final Text text, final int span) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = span;
		text.setLayoutData(data);
		
		text.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				dialogChanged();
			}
		});
	}
	
	private void initialize() {
		nameText.setText("MyCapability");
		descriptionText.setText("My Capability Description");
		idText.setText("edu.washington.cs.cupid.custom.mycapability");
		parameterType.setText(DEFAULT_TYPE);
		outputType.setText(DEFAULT_TYPE);
	}

	private void dialogChanged() {

		String name = getCapabilityName();
		String description = getCapabilityDescription();
		String id = getUniqueId();

		if (name.isEmpty()) {
			updateStatus("Capability name must be specified");
			return;
		}
		
		if (id.isEmpty()) {
			updateStatus("Unique id must be specified");
			return;
		}
		
		if (description.isEmpty()) {
			updateStatus("Capability description must be specified");
			return;
		}
		
		try {
			getParameterType();
		} catch (ClassNotFoundException e) {
			updateStatus("Unknown parameter type " + parameterType.getText());
			return;
		}
		
		try {
			getReturnType();
		} catch (ClassNotFoundException e) {
			updateStatus("Unknown return type " + outputType.getText());
			return;
		}
		
		updateStatus(null);
	}
	
	private void validateName() {
		String name =  nameText.getText();
		boolean isValid = name.matches("[a-zA-Z][a-zA-Z1-9 ]*");
		
		if (isValid) {
			updateStatus(null);
		} else {
			updateStatus("Invalid capability name");
		}
	}

	private void updateStatus(final String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * Returns the resolved parameter type for the new capability.
	 * @return the resolved parameter type for the new capability
	 * @throws ClassNotFoundException if the user supplied type cannot be resolved
	 */
	public Class<?> getParameterType() throws ClassNotFoundException {
		return Class.forName(parameterType.getText());
	}
	
	/**
	 * Returns the resolved return type for the new capability.
	 * @return the resolved return type for the new capability
	 * @throws ClassNotFoundException if the user supplied type cannot be resolved
	 */
	public Class<?> getReturnType() throws ClassNotFoundException {
		return Class.forName(outputType.getText());
	}
	
	/**
	 * Returns the path (e.g., jar) that defines the parameter type.
	 * @return the path (e.g., jar) that defines the parameter type
	 */
	public IPath getParameterTypeReference() {
		return parameterTypeReference;
	}

	/**
	 * Returns the path (e.g., jar) that defines the output type.
	 * @return the path (e.g., jar) that defines the output type
	 */
	public IPath getOutputTypeReference() {
		return outputTypeReference;
	}

	/**
	 * Returns the unique id for the new capability.
	 * @return the unique id for the new capability
	 */
	public String getUniqueId() {
		return idText.getText();
	}
	
	/**
	 * Returns the name for the new capability.
	 * @return the name for the new capability
	 */
	public String getCapabilityName() {
		return nameText.getText();
	}
	
	/**
	 * Returns the description for the new capability.
	 * @return the description for the new capability
	 */
	public String getCapabilityDescription() {
		return descriptionText.getText();
	}
}