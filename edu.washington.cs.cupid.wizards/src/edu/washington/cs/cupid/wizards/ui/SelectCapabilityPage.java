package edu.washington.cs.cupid.wizards.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class SelectCapabilityPage extends WizardPage {

	private Composite container = null;
	private Class<?> startType;
	
	protected SelectCapabilityPage(Class<?> startType) {
		super("Select a capability");
		this.startType = startType;
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		
		Group existing = new Group(container, SWT.BORDER);
		Combo matching = new Combo(existing, SWT.DROP_DOWN | SWT.READ_ONLY); 
		matching.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		existing.setText("Select Capability");
		
		Group getters = new Group(container, SWT.BORDER);
		existing.setText("Select Accessor");
		SelectAccessorWidget selectGetter = new SelectAccessorWidget(getters, startType, SWT.NONE);
		selectGetter.setLayoutData(new GridData(SWT.FILL));
	}
}
