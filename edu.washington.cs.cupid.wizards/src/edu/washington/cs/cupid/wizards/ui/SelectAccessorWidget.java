package edu.washington.cs.cupid.wizards.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.wizards.TypeComboUpdater;
import edu.washington.cs.cupid.wizards.TypeUtil;
import edu.washington.cs.cupid.wizards.internal.Activator;
import edu.washington.cs.cupid.wizards.internal.DerivedCapability;
import edu.washington.cs.cupid.wizards.internal.Getter;

public class SelectAccessorWidget extends Composite {

	private Combo type;

	private TreeViewer methodTree;
	private TableRow selected;
	private ViewContentProvider treeContent = new ViewContentProvider();

	private List<ModifyListener> listeners = Lists.newArrayList();
	
	public SelectAccessorWidget(Composite parent, Class<?> startClazz, int style) {
		super(parent, style);
		
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		this.setLayout(layout);
		
		this.type = new Combo(this, SWT.LEFT | SWT.BORDER);
		this.type.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.type.setText(startClazz.getName());
		
		final TypeComboUpdater updater = new TypeComboUpdater(type);
		updater.updateSuperTypeList(startClazz.getName());
		
		Button search = new Button(this, SWT.PUSH);
		search.setText("Select");
		search.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IType selected = TypeUtil.showTypeDialog(getShell());
					if (selected != null){
						String newType = selected.getFullyQualifiedName();
						type.setText(newType);
						updater.updateSuperTypeList(newType);
					}
				} catch (Exception ex) {
					throw new RuntimeException("Error opening type search dialog", ex);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
		
		this.type.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				methodTree.setInput(type.getText());
				methodTree.refresh();
			}
		});
		
		final Tree tree = new Tree(this, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
		
		TreeColumn cMethod = new TreeColumn(tree, SWT.LEFT);
		cMethod.setText("Method");
		
		TreeColumn cType = new TreeColumn(tree, SWT.LEFT);
		cType.setText("Type");
		
		this.methodTree = new TreeViewer(tree);
		this.methodTree.setContentProvider(treeContent);
		this.methodTree.setLabelProvider(new ViewLabelProvider());
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 300;
		data.horizontalSpan = 2;
		tree.setLayoutData(data);
		
		TableLayout treeLayout = new TableLayout();
		treeLayout.addColumnData(new ColumnWeightData(1));
		treeLayout.addColumnData(new ColumnWeightData(1));
		tree.setLayout(treeLayout);
		
		tree.setHeaderVisible(true);
		
		tree.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				selected = (TableRow) tree.getSelection()[0].getData();
				onModify();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// NO OP
			}
		});
	}
	
	private void onModify(){
		Event e = new Event();
		e.data = selected;
		e.widget = this;
		e.item = this;
		
		ModifyEvent m = new ModifyEvent(e); 
		
		for (final ModifyListener listener : listeners){
			listener.modifyText(m);
		}
	}
	
	public void addModifyListener(ModifyListener listener){
		listeners.add(listener);
	}
	
	public boolean hasSelection(){
		return selected != null;
	}

	public Getter<?,?> getGetter() throws Exception {
		Class<?> clazz = Activator.getDefault().getBundle().loadClass(type.getText());
		
		List<String> fields = Lists.newArrayList();
		TableRow row = selected;
		while (row != null){
			fields.add(0, row.method.getName());
			row = row.parent;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Getter<?, ?> result = new Getter(fields, TypeToken.of(clazz), TypeManager.boxType(TypeToken.of(selected.method.getGenericReturnType())));
		
		return result;
	}
	
	private class TableRow{
		private final TableRow parent;
		private final Method method;
		
		private TableRow(TableRow parent, Method method) {
			this.parent = parent;
			this.method = method;
		}
	}
	
	private static Method[] getters(Class<?> clazz){
		ArrayList<Method> result = Lists.newArrayList();
		
		for (Method m : clazz.getMethods()){
			if (DerivedCapability.isGetter(m)){
				result.add(m);	
			}
		}
		
		Collections.sort(result, new Comparator<Method>(){
			@Override
			public int compare(Method lhs, Method rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		
		return result.toArray(new Method[]{});
	}
	
	private final class ViewContentProvider implements ITreeContentProvider {
		@Override
		public void dispose() {
			// NO OP
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// NO OP
		}

		@Override
		public Object[] getElements(Object inputElement) {
			Class<?> clazz;
			try {
				clazz = Activator.getDefault().getBundle().loadClass((String) inputElement);
			} catch (ClassNotFoundException e) {
				return new Object[]{};
			}
			ArrayList<TableRow> elements = Lists.newArrayList();
			for (Method getter : getters(clazz)){
				elements.add(new TableRow(null, getter));
			}
			return elements.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			TableRow row = (TableRow) parentElement;
			
			ArrayList<TableRow> elements = Lists.newArrayList();
			for (Method getter : getters(row.method.getReturnType())){
				elements.add(new TableRow(row, getter));
			}
			return elements.toArray();
		}

		@Override
		public Object getParent(Object element) {
			return ((TableRow) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			TableRow row = (TableRow) element;
			Class<?> type = row.method.getReturnType();
			return !type.isPrimitive() && !type.equals(Object.class) && type.getMethods().length != 0;
		}
	}
	
	private final class ViewLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			// NO OP
		}

		@Override
		public void dispose() {
			// NO OP
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// NO OP
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex){
			case 0: 
				return ((TableRow) element).method.getName();
			case 1: 
				return TypeManager.simpleTypeName(((TableRow) element).method.getGenericReturnType());
			default:
				return null;
			}
		}
	}
}
