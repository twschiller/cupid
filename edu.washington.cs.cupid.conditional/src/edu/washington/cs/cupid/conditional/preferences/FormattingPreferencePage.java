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
package edu.washington.cs.cupid.conditional.preferences;

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.exception.MalformedCapabilityException;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.conditional.Formatter;
import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.internal.Activator;

/**
 * Preference page for defining and editing conditional formatting rules.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class FormattingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	// TODO implement restore defaults
	// TODO add font override support
	// TODO handle cancel event from color dialog
	
	private Composite composite;
	private Table table;
	private ToolBar toolbar;
	private Group editor;
	
	/**
	 * Store the selected item, so we can tell if it changes.
	 */
	private TableItem active;
	
	/**
     * True iff the user hasn't selected a capability yet for {@link active}, the selected item.
     */
    private boolean containsSentinalEntry = false;
	
	/**
	 * List of currently available predicates; currently updated each time the user 
	 * selects a formatting rule.
	 */
    private List<ICapability> available;
    
    /**
     * Construct the preference page for defining and editing conditional formatting rules.
     */
	public FormattingPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Conditional Formatting Rules");
	}
	
	@Override
	public void init(final IWorkbench workbench) {
		available = Lists.newArrayList(CupidPlatform.getCapabilityRegistry().getPredicates());
	}

	private void createEditForm(final Composite parent, final TableItem item) {
		
		final FormattingRule rule = (FormattingRule) item.getData();
		
		for (Control control : parent.getChildren()) {
			control.dispose();
		}
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginWidth  = 5;
		layout.marginHeight  = 5;
		parent.setLayout(layout);
		
		final ColorDialog color = new ColorDialog(Display.getCurrent().getActiveShell());
		final FontDialog font = new FontDialog(Display.getCurrent().getActiveShell());
		
		Label lName = new Label(parent, SWT.NONE);
		lName.setText("Name:");
		
		final Text tName = new Text(parent, SWT.SINGLE | SWT.BORDER);
		tName.setText(rule.getName());
		tName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				rule.setName(tName.getText());
				item.setText(rule.getName());
			}
		});
		tName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label lBackground = new Label(parent, SWT.NONE);
		lBackground.setText("Background:");
		
		final Button bBackground = new Button(parent, SWT.PUSH);
		bBackground.setLayoutData(new GridData(60,30));
		if (rule.getFormat().getBackground() != null) {
			setButtonColor(bBackground, rule.getFormat().getBackground());
		}
		
		bBackground.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(final MouseEvent e) {
				RGB choice = color.open();
				setButtonColor(bBackground, choice);
				rule.getFormat().setBackground(choice);
				item.setBackground(new Color(Display.getDefault(), choice));
			}
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// NO OP
			}
			@Override
			public void mouseUp(final MouseEvent e) {
				// NO OP
			}
		});
		
		
		Label lCapability = new Label(parent, SWT.NONE);
		lCapability.setText("Capability:");
		createPredicateList(parent, rule);
		
		Label lForeground = new Label(parent, SWT.NONE);
		lForeground.setText("Foreground:");
		
		final Button bForeground = new Button(parent, SWT.PUSH);
		bForeground.setLayoutData(new GridData(60, 30));
		if (rule.getFormat().getForeground() != null) {
			setButtonColor(bForeground, rule.getFormat().getForeground());
		}
		bForeground.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(final MouseEvent e) {
				RGB choice = color.open();
				setButtonColor(bForeground, choice);
				rule.getFormat().setForeground(choice);
				item.setForeground(new Color(Display.getDefault(), choice));
			}
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// NO OP
			}
			@Override
			public void mouseUp(final MouseEvent e) {
				// NO OP
			}
		});
		
		Label lFont = new Label(parent, SWT.NONE);
		lFont.setText("Font:");
		
		final Button bFont = new Button(parent, SWT.PUSH);
		bFont.setText("Select Font");
		if (rule.getFormat().getFont() != null) {
			bFont.setFont(new Font(Display.getDefault(), rule.getFormat().getFont()));
		}
		
		bFont.addMouseListener(new MouseListener() {
			@Override
			public void mouseDown(final MouseEvent e) {
				if (font.open() != null) {
					FontData[] choice = font.getFontList();
					rule.getFormat().setFont(choice);
					Font font = new Font(Display.getDefault(), choice);
					bFont.setFont(font);
					item.setFont(font);
				}
			}
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// NO OP
			}
			@Override
			public void mouseUp(final MouseEvent e) {
				// NO OP
			}
		});
		
		parent.layout(true);
		composite.layout(true);
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
	
	/**
	 * Populate the predicate option list for the formatting rules.
	 * @param parent the parent widget
	 * @param rule the formatting rule
	 */
	private void createPredicateList(final Composite parent, final FormattingRule rule) {
		final Combo cCapability = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);

		if (available.isEmpty()) {
			addAndSet(cCapability, "No predicates available");
		} else {
			ICapability forRule = null;

			if (rule.getCapabilityId() != null) {
				try {
					forRule = Activator.findPredicate(rule);
					containsSentinalEntry = false;
				} catch (NoSuchCapabilityException e) {
					addAndSet(cCapability, "Unknown capability");
					containsSentinalEntry = true;
				} catch (MalformedCapabilityException e) {
					addAndSet(cCapability, "Invalid predicate");
					containsSentinalEntry = true;
				}
			} else {
				addAndSet(cCapability, "Select predicate");
				containsSentinalEntry = true;
			}

			for (ICapability capability : available ) {
				if (forRule == capability) {
					addAndSet(cCapability, capability.getName());
				} else {
					cCapability.add(capability.getName());
				}
			}

			// TODO handle formats with capabilities that don't exist
		}

		cCapability.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (!available.isEmpty()) {
					if (containsSentinalEntry) {
						if (cCapability.getSelectionIndex() > 0) {
							rule.setCapabilityId(available.get(cCapability.getSelectionIndex() - 1).getUniqueId());
							cCapability.remove(0);
							containsSentinalEntry = false;
						}
					} else {
						rule.setCapabilityId(available.get(cCapability.getSelectionIndex()).getUniqueId());
					}
				}
			}
		});

		cCapability.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	@Override
	public boolean performOk() {
		save();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		disableAll();
	}
	
	@Override
	protected Control createContents(final Composite parent) {	
		composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginRight = 5;
		layout.marginTop = 5;
		layout.marginWidth  = 0;
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		toolbar = new ToolBar(composite, SWT.HORIZONTAL | SWT.FLAT);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		toolbar.setLayoutData(data);
		
		final ToolItem add = new ToolItem(toolbar, SWT.PUSH);
		add.setText("Add");
		add.setToolTipText("Add Rule");
		add.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				FormattingRule rule = new FormattingRule("My Rule");
				TableItem item = addRuleItem(rule);
				table.select(table.getItemCount() - 1);
				createEditForm(editor, item);
				active = item;
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem copy = new ToolItem(toolbar, SWT.PUSH);
		copy.setText("Copy");
		copy.setToolTipText("Copy Rule");
		copy.setEnabled(false);
		copy.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				TableItem item = table.getSelection()[0];
				
				FormattingRule clone = ((FormattingRule) item.getData()).copy();
				clone.setName(clone.getName() + " (Copy) ");
				TableItem cloned = addRuleItem(clone);
				table.select(table.getItemCount() - 1);
				createEditForm(editor, cloned);
				active = item;
				copy.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem delete = new ToolItem(toolbar, SWT.PUSH);
		delete.setText("Delete");
		delete.setToolTipText("Delete Rule");
		delete.setEnabled(false);
		delete.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// TODO handle multiple selections
				table.remove(table.getSelectionIndex());
				delete.setEnabled(false);
				
				for (Control control : editor.getChildren()) {
					control.dispose();
				}
				editor.layout(true);
				composite.layout(true);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem enable = new ToolItem(toolbar, SWT.PUSH);
		enable.setText("Enable");
		enable.setToolTipText("Enable All");
		enable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				// TODO handle multiple selections
				for (TableItem item : table.getItems()) {
					item.setChecked(true);
					((FormattingRule) item.getData()).setActive(true);
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		final ToolItem disable = new ToolItem(toolbar, SWT.PUSH);
		disable.setText("Disable");
		disable.setToolTipText("Disable All");
		disable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				disableAll();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		table.setLayoutData(data);
		table.setHeaderVisible(false);
		
		String[] titles = { "Formatting Rule" };
		
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NULL);
			column.setText(titles[i]);
		}
		
		FormattingRule[] rules = new FormattingRule[]{};
		try {
			rules = Activator.getDefault().storedRules();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
				
		for (FormattingRule rule : rules) {
			addRuleItem(rule);
		}
	
		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}
		
		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				TableItem item = (TableItem) e.item;
		
				FormattingRule rule = ((FormattingRule) item.getData());
				rule.setActive(item.getChecked());
		
				if (active != item) {
					createEditForm(editor, item);
					active = item;
				}
				
				delete.setEnabled(true);
				copy.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				// NO OP
			}
		});
		
		if (table.getItemCount() > 0) {
			table.select(0);
		}
		
		editor = new Group(composite, SWT.NONE);
		editor.setText("Edit Formatting Rule");
		data = new GridData(GridData.FILL_HORIZONTAL);
		layout.marginWidth  = 5;
		layout.marginHeight = 5;
		editor.setLayoutData(data);
		
		return null;
	}
	
	private void disableAll() {
		// TODO handle multiple selections
		for (TableItem item : table.getItems()) {
			item.setChecked(false);
			((FormattingRule) item.getData()).setActive(false);
		}
	}
	
	/**
	 * Add <code>rule</code> to the table, using the formatting described by <code>rule</code>.
	 * @param rule the rule
	 * @return the added item
	 */
	private TableItem addRuleItem(final FormattingRule rule) {
		TableItem item = new TableItem(table, SWT.NULL);
		item.setText(rule.getName());
		item.setText(0, rule.getName());
		item.setChecked(rule.isActive());
		item.setData(rule);
		
		Formatter.applyFormat(item, rule.getFormat());	
		
		return item;
	}
	
	/**
	 * Set the image of <code>button</code> to a solid <code>color</code> rectangle. Does
	 * nothing if color is <code>null</code>.
	 * @param button the button
	 * @param color the color
	 */
	private static void setButtonColor(final Button button, final RGB color) {
		if (color != null) {
			Device display = Display.getDefault();
			Image image = new Image(display, 30, 20);	
			GC gc = new GC(image);
			gc.setBackground(new Color(display, color));
			gc.fillRectangle(image.getBounds());
			button.setImage(image);
		}
	}
	
	/**
	 * Save the formatting rules to the preference store in JSON format.
	 */
	private void save() {
		Gson gson = new Gson();
		FormattingRule[] rules = new FormattingRule[table.getItemCount()];
		for (int i = 0; i < rules.length; i++) {
			rules[i] = (FormattingRule) table.getItem(i).getData();
		}
		Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_RULES, gson.toJson(rules));
	}

	
	
}
