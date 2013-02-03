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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.options.IConfigurableCapability;
import edu.washington.cs.cupid.capability.options.Option;
import edu.washington.cs.cupid.internal.CupidActivator;

/**
 * A factory that builds capability option editors.
 * @author Todd Schiller
 */
public final class OptionEditorFactory {

	private OptionEditorFactory() {
		// NO OP
	}
	
	/**
	 * A capability option editor.
	 * @author Todd Schiller
	 * @param <T> the type of capability option
	 */
	public interface OptionEditor<T> {
		/**
		 * Returns the current user-provided value for the option.
		 * @return the current user-provided value for the option.
		 */
		T getValue();
		
		/**
		 * Returns the associated option.
		 * @return the associated optio
		 */
		Option<T> getOption();
		
		/**
		 * Returns the underlying widget.
		 * @return the underlying widget
		 */
		Widget getWidget();
		
		/**
		 * Creates the editor in <code>parent</code> assuming a {@link GridLayout}.
		 * @param parent the parent composite
		 * @param showDefault <code>true</code> indicates the default option value should be shown
		 */
		void create(Composite parent, boolean showDefault);
	}
	
	/**
	 * Returns an editor for the given capability and option.
	 * @param capability the capability
	 * @param option the option
	 * @return an editor for the given capability and option.
	 */
	@SuppressWarnings("unchecked")
	public static OptionEditor<?> getEditor(final IConfigurableCapability<?, ?> capability, final Option<?> option) {
		if (option.getType().equals(TypeToken.of(Integer.class))) {
			return new IntegerInput(capability, (Option<Integer>) option);
		} else if (option.getType().equals(TypeToken.of(Boolean.class))) {
			return new BooleanInput(capability, (Option<Boolean>) option);
		} else if (option.getType().equals(TypeToken.of(String.class))) {
			return new StringInput(capability, (Option<String>) option);
		} else if (option.getType().equals(TypeToken.of(Double.class))) {
			return new DoubleInput(capability, (Option<Double>) option);
		}
		return null;
	}
	
	/**
	 * An abstract base class for {@link OptionEditor}s.
	 * @author Todd Schiller
	 * @param <T> the option type
	 */
	public abstract static class AbstractOptionEditor<T> implements OptionEditor<T> {
		private final IConfigurableCapability<?, ?> capability;
		private final Option<T> option;
		
		/**
		 * An abstract base class for {@link OptionEditor}s that stores the associated capability and option.
		 * @param capability the associated capability
		 * @param option the associated option
		 */
		public AbstractOptionEditor(final IConfigurableCapability<?, ?> capability, final Option<T> option) {
			this.capability = capability;
			this.option = option;
		}

		@Override
		public final Option<T> getOption() {
			return option;
		}

		/**
		 * Returns the associated capability.
		 * @return the associated capability.
		 */
		public final IConfigurableCapability<?, ?> getCapability() {
			return capability;
		}
	}

	private static final class IntegerInput extends AbstractOptionEditor<Integer> {
		private Text field;
		
		private IntegerInput(final IConfigurableCapability<?, ?> capability, final Option<Integer> option) {
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
		public void create(final Composite parent, final boolean showDefault) {
			field = new Text(parent, SWT.BORDER);
			field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			Integer def = showDefault 
					? getOption().getDefault()
					: CupidActivator.getDefault().getCapabilityOptions().get(getCapability(), getOption());
			
			if (def != null){
				field.setText(def.toString());
			}
		}
	}
	
	private static final class DoubleInput extends AbstractOptionEditor<Double> {
		private Text field;
		
		private DoubleInput(final IConfigurableCapability<?, ?> capability, final Option<Double> option) {
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
		public void create(final Composite parent, final boolean showDefault) {
			field = new Text(parent, SWT.BORDER);
			field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			Double def = showDefault 
					? getOption().getDefault()
					: CupidActivator.getDefault().getCapabilityOptions().get(getCapability(), getOption());
			
			if (def != null){
				field.setText(def.toString());
			}
		}
	}

	private static final class StringInput extends AbstractOptionEditor<String> {
		private Text field;
		
		private StringInput(final IConfigurableCapability<?, ?> capability, final Option<String> option) {
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
		public void create(final Composite parent, final boolean showDefault) {
			field = new Text(parent, SWT.BORDER);
			field.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			String def = showDefault 
					? getOption().getDefault()
					: CupidActivator.getDefault().getCapabilityOptions().get(getCapability(), getOption());
			
			if (def != null){
				field.setText(def.toString());
			}
		}
	}
		
	private static final class BooleanInput extends AbstractOptionEditor<Boolean> {
		private Composite composite;
		private Button trueButton;
		
		private BooleanInput(final IConfigurableCapability<?, ?> capability, final Option<Boolean> option) {
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
		public void create(final Composite parent, final boolean showDefault) {
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
			
			Boolean current = showDefault 
					? getOption().getDefault()
					: CupidActivator.getDefault().getCapabilityOptions().get(getCapability(), getOption());
			trueButton.setSelection(current);
			falseButton.setSelection(!current);
		}
	}	
}
