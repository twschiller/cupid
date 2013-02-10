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
package edu.washington.cs.cupid.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IParameter;

/**
 * A factory that builds capability option editors.
 * @author Todd Schiller
 */
public final class OptionEditorFactory {

	private OptionEditorFactory() {
		// NO OP
	}
	
	public interface ValueChangedListener<T>{
		void valueChanged(OptionEditor<T> option, T value);
	}
	
	/**
	 * A capability option editor.
	 * @author Todd Schiller
	 * @param <T> the type of capability option
	 */
	public interface OptionEditor<T> {
		
		void addValueChangedListener(ValueChangedListener<T> listener);
		
		/**
		 * Returns the current user-provided value for the option.
		 * @return the current user-provided value for the option.
		 */
		T getValue();
		
		/**
		 * Returns the associated option.
		 * @return the associated option
		 */
		IParameter<T> getOption();
		
		/**
		 * Returns the underlying widget.
		 * @return the underlying widget
		 */
		Widget getWidget();
		
		/**
		 * Creates the editor in <code>parent</code> assuming a {@link GridLayout}.
		 * @param parent the parent composite
		 */
		void create(Composite parent, T initialValue);
	}
	
	/**
	 * Returns an editor for the given capability and option.
	 * @param capability the capability
	 * @param option the option
	 * @return an editor for the given capability and option.
	 */
	public static OptionEditor<?> getEditor(final ICapability capability, final IParameter option) {
		if (option.getType().equals(TypeToken.of(Integer.class))) {
			return new IntegerInput(capability, option);
		} else if (option.getType().equals(TypeToken.of(Boolean.class))) {
			return new BooleanInput(capability, option);
		} else if (option.getType().equals(TypeToken.of(String.class))) {
			return new StringInput(capability, option);
		} else if (option.getType().equals(TypeToken.of(Double.class))) {
			return new DoubleInput(capability, option);
		}
		return null;
	}
	
	/**
	 * An abstract base class for {@link OptionEditor}s.
	 * @author Todd Schiller
	 * @param <T> the option type
	 */
	public abstract static class AbstractOptionEditor<T> implements OptionEditor<T> {
		private final ICapability capability;
		private final IParameter<T> option;
		private final List<ValueChangedListener<T>> listeners;
		
		/**
		 * An abstract base class for {@link OptionEditor}s that stores the associated capability and option.
		 * @param capability the associated capability
		 * @param option the associated option
		 */
		public AbstractOptionEditor(final ICapability capability, final IParameter<T> option) {
			this.capability = capability;
			this.option = option;
			this.listeners = Lists.newArrayList();
		}

		@Override
		public final IParameter<T> getOption() {
			return option;
		}

		/**
		 * Returns the associated capability.
		 * @return the associated capability.
		 */
		public final ICapability getCapability() {
			return capability;
		}

		@Override
		public void addValueChangedListener(ValueChangedListener<T> listener) {
			listeners.add(listener);
		}
		
		protected void alert(){
			for (ValueChangedListener<T> listener : listeners){
				listener.valueChanged(this, getValue());
			}
		}
	}

	private static final class IntegerInput extends AbstractOptionEditor<Integer> {
		private Text field;
		
		private IntegerInput(final ICapability capability, final IParameter<Integer> option) {
			super(capability, option);
		}

		@Override
		public Integer getValue() {
			if (field.getText() == null || field.getText().isEmpty()) {
				return null;
			} else {
				return Integer.parseInt(field.getText());
			}
		}

		@Override
		public Widget getWidget() {
			return field;
		}

		@Override
		public void create(final Composite parent, Integer initialValue) {
			field = new Text(parent, SWT.BORDER);
			field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			Integer def = initialValue;
					
			if (def != null){
				field.setText(def.toString());
			}
			
			field.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					alert();
				}
			});
		}
	}
	
	private static final class DoubleInput extends AbstractOptionEditor<Double> {
		private Text field;
		
		private DoubleInput(final ICapability capability, final IParameter<Double> option) {
			super(capability, option);
		}

		@Override
		public Double getValue() {
			if (field.getText() == null || field.getText().isEmpty()) {
				return null;
			} else {
				return Double.parseDouble(field.getText());
			}
		}

		@Override
		public Widget getWidget() {
			return field;
		}

		@Override
		public void create(final Composite parent, Double initialValue) {
			field = new Text(parent, SWT.BORDER);
			field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			Double def = initialValue;
					
			if (def != null){
				field.setText(def.toString());
			}
			
			field.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					alert();
				}
			});
		}
	}

	private static final class StringInput extends AbstractOptionEditor<String> {
		private Text field;
		
		private StringInput(final ICapability capability, final IParameter<String> option) {
			super(capability, option);
		}

		@Override
		public String getValue() {
			return field.getText();
		}

		@Override
		public Widget getWidget() {
			return field;
		}

		@Override
		public void create(final Composite parent, String initialValue) {
			field = new Text(parent, SWT.BORDER);
			field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			String def = initialValue;
					
			if (def != null){
				field.setText(def.toString());
			}
			
			field.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					alert();
				}
			});
		}
	}
		
	private static final class BooleanInput extends AbstractOptionEditor<Boolean> {
		private Composite composite;
		private Button trueButton;
		
		private BooleanInput(final ICapability capability, final IParameter<Boolean> option) {
			super(capability, option);
		}

		@Override
		public Boolean getValue() {
			return trueButton.getSelection();
		}

		@Override
		public Widget getWidget() {
			return composite;
		}

		@Override
		public void create(final Composite parent, Boolean initialValue) {
			composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			composite.setLayout(layout);
			
			trueButton = new Button(composite, SWT.RADIO);
			trueButton.setText("True");
			trueButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			
			Button falseButton = new Button(composite, SWT.RADIO);
			falseButton.setText("False");
			falseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			
			Boolean current = initialValue;
			
			trueButton.setSelection(current);
			falseButton.setSelection(!current);
			
			trueButton.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					alert();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// NO OP
				}
			});
		}
	}	
}