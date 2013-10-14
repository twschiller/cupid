package edu.washington.cs.cupid.conditional.ui;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;

public class ColorPickerButton extends Composite {
	
	private final Button button;
	
	private RGB color;
	private final List<ChangeListener> listeners = Lists.newArrayList();
	private final ColorDialog dialog;
	
	public ColorPickerButton(Composite parent, RGB color) {
		super(parent, SWT.NONE);
		
		this.dialog = new ColorDialog(parent.getShell());
		
		this.setLayout(new GridLayout());
		this.button = new Button(this, SWT.PUSH);
		this.setButtonColor(color);
		this.button.addMouseListener(new ClickListener());
		this.button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	public void addChangeListener(ChangeListener listener){
		listeners.add(listener);
	}

	/**
	 * Set the image of <code>button</code> to a solid <code>color</code> rectangle. Does
	 * nothing if color is <code>null</code>.
	 * @param button the button
	 * @param color the color
	 */
	private void setButtonColor(final RGB color) {
		this.color = color;
		if (color != null) {
			Device display = Display.getDefault();
			Image image = new Image(display, 30, 20);	
			GC gc = new GC(image);
			gc.setBackground(new Color(display, color));
			gc.fillRectangle(image.getBounds());
			button.setImage(image);
		}
	}
	
	public RGB getColor(){
		return color;
	}
	
	private class ClickListener implements MouseListener{

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// NO OP
		}

		@Override
		public void mouseDown(MouseEvent e) {
			RGB choice = dialog.open();
			setButtonColor(choice);	
			ChangeEvent event = new ChangeEvent(ColorPickerButton.this);
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
