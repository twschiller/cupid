package edu.washington.cs.cupid.wizards.ui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.washington.cs.cupid.conditional.Format;
import edu.washington.cs.cupid.conditional.ui.ColorPickerButton;
import edu.washington.cs.cupid.conditional.ui.FontPickerButton;

public class FormatPage extends WizardPage {

	private ColorPickerButton bBackground;
	private ColorPickerButton bForeground;
	private FontPickerButton bFont;
	private Text tName;
	
	protected FormatPage() {
		super("Choose Format");
		this.setTitle("Select formatting");
		this.setMessage("Specify the rule name and one or more format overrides");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		this.setPageComplete(false);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 6;
		container.setLayout(layout);
		
		Label lName = new Label(container, SWT.LEFT);
		lName.setText("Rule Name (Optional):");
		tName = new Text(container, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
		GridData dName = new GridData(SWT.FILL, SWT.NONE, true, false);
		dName.horizontalSpan = 5;
		tName.setLayoutData(dName);
		
		Label lBackground = new Label(container, SWT.LEFT);
		lBackground.setText("Background Color:");
		bBackground = new ColorPickerButton(container, null);
		bBackground.setLayoutData(new GridData(60, 50));
		bBackground.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				if (bBackground.getColor() != null){
					FormatPage.this.setPageComplete(true);
				}
			}
		});
		
		Label lForeground = new Label(container, SWT.LEFT);
		lForeground.setText("Foreground Color:");
		bForeground = new ColorPickerButton(container, null);	
		bForeground.setLayoutData(new GridData(60, 50));
		bForeground.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				if (bForeground.getColor() != null){
					FormatPage.this.setPageComplete(true);
				}
			}
		});
		
		Label lFont = new Label(container, SWT.LEFT);
		lFont.setText("Font:");
		bFont = new FontPickerButton(container, null);
		bFont.setText("Select Font");
		bFont.setLayoutData(new GridData(60, 50));
		
		bFont.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				if (bFont.getFontData() != null){
					FormatPage.this.setPageComplete(true);
				}
			}
		});
		
		this.setControl(container);
	}
	
	public Format getFormat(){
		return new Format(bForeground.getColor(), bBackground.getColor(), bFont.getFontData());
	}
	
	public String getName(){
		String trimmed = tName.getText().trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
