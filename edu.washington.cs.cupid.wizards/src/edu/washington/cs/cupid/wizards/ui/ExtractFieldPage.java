package edu.washington.cs.cupid.wizards.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.DerivedCapability;
import edu.washington.cs.cupid.wizards.internal.Getter;

public class ExtractFieldPage extends WizardPage {

	private String startClazz;
	
	protected ExtractFieldPage(String clazz) {
		super("Extract Field");
		this.startClazz = clazz;
	}
	
	private List methodList;
	private Text type;
	private String method;

	@Override
	public void createControl(Composite parent) {
		this.setTitle("Extract information");
		this.setMessage("Select a type then select a getter or field");
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		composite.setLayout(layout);
		
		type = new Text(composite, SWT.LEFT | SWT.BORDER);
		type.setText(startClazz);
		data = new GridData(GridData.FILL_HORIZONTAL);
		type.setLayoutData(data);
		
		Button search = new Button(composite, SWT.PUSH);
		search.setText("Select");
		search.addMouseListener(new MouseListener(){
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
				// TODO add selection dialog
			}
		});
		
		type.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateMethodList();
			}
		});
		
		methodList = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 300;
		data.horizontalSpan = 2;
		methodList.setLayoutData(data);
	
		methodList.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				method = methodList.getSelection()[0];
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		updateMethodList();
		
		setControl(composite);
	}
	
	private void updateMethodList(){
		methodList.removeAll();
		
		Class<?> clazz;
		try {
			clazz = Activator.getDefault().getBundle().loadClass(type.getText());
		} catch (ClassNotFoundException e) {
			return;
		}
		
		ArrayList<String> names = Lists.newArrayList();
		
		for (Method m : clazz.getMethods()){
			if (DerivedCapability.isGetter(m)){
				names.add(m.getName());
				
			}
		}
		
		Collections.sort(names);
		
		for (String name : names){
			methodList.add(name);
		}
	}
	
	public Getter getGetter(){
		Class<?> clazz;
		try {
			clazz = Activator.getDefault().getBundle().loadClass(type.getText());
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		try {
			return new Getter(method, TypeToken.of(clazz), TypeToken.of(clazz.getMethod(method).getReturnType()));
		} catch (Exception e) {
			return null;
		}
	}
}
