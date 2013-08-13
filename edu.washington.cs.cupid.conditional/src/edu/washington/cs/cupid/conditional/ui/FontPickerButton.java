package edu.washington.cs.cupid.conditional.ui;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;

import com.google.common.collect.Lists;

public class FontPickerButton extends Composite {

	private FontData[] fontData;
	
	private final Button button;
	private List<ChangeListener> listeners = Lists.newArrayList();
	final FontDialog dialog = new FontDialog(Display.getCurrent().getActiveShell());
	
	public FontPickerButton(Composite parent, FontData[] fontData) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		this.button = new Button(this, SWT.PUSH);
		this.setButtonFont(fontData);
		this.button.addMouseListener(new ClickListener());
		this.button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	public void addChangeListener(ChangeListener listener){
		listeners.add(listener);
	}
	
	public FontData[] getFontData(){
		return fontData;
	}
	
	private void setButtonFont(FontData[] fontData){
		this.fontData = fontData;
		if (fontData != null) {
			Font f = new Font(Display.getDefault(), fontData);
			button.setFont(f);
		}
	}
	
	public void setText(String text){
		button.setText(text);
	}
	
	private class ClickListener implements MouseListener{

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// NO OP
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (dialog.open() != null) {
				FontData[] choice = dialog.getFontList();
				setButtonFont(choice);
			}
			
			ChangeEvent event = new ChangeEvent(FontPickerButton.this);
			for (ChangeListener listener : listeners){
				listener.stateChanged(event);
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			// NO OP
		}
	}

}
