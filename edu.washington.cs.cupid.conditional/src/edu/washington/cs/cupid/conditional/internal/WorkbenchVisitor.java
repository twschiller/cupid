package edu.washington.cs.cupid.conditional.internal;

import java.lang.reflect.Method;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class WorkbenchVisitor {

	private final Class<?> workbenchPartReferenceClazz;
	private final Class<?> partPaneClazz;
	private final Method getPaneMethod;
	private final Method getControlMethod;
	
	public WorkbenchVisitor() throws Exception{
		this.workbenchPartReferenceClazz = Class.forName("org.eclipse.ui.internal.WorkbenchPartReference");
		this.partPaneClazz = Class.forName("org.eclipse.ui.internal.PartPane");
		this.getPaneMethod = workbenchPartReferenceClazz.getMethod("getPane");
		this.getControlMethod = partPaneClazz.getMethod("getControl");
	}
	
	public void visit(final Tree tree){
		for (final TreeItem item : tree.getItems()) {
			visit(tree, item);
		}
	}
	
	public void visit(final Tree tree, final TreeItem item){
		for (final TreeItem child : item.getItems()){
			visit(tree, child);
		}
	}
	
	public void visit(final Table table, final TableItem item){
		// NOP
	}
	
	public void visit(final Composite composite){
		for (Control child : composite.getChildren()){
			dispatch(child);
		}
	}
	
	public void visit(final Table table){
		for (final TableItem item : table.getItems()) {
			visit(table, item);
		}
	}
	
	public void visit(final Control control){
		// NOP
	}
	
	private void dispatch(final Control control){
		if (control instanceof Table){
			visit((Table) control);
		}else if (control instanceof Tree){
			visit((Tree) control);
		}else if (control instanceof Composite){
			visit((Composite) control);
		}else{
			visit(control);
		}
	}
	
	public void visit(final IWorkbenchWindow window){
		for (IWorkbenchPage page: window.getPages()){
			visit(page);
		}
	}
	
	public void visit(final IWorkbench workbench){
		for (IWorkbenchWindow window: workbench.getWorkbenchWindows()){
			visit(window);
		}
	}
	
	public void visit(final IWorkbenchPage page){
		for (IViewReference viewRef : page.getViewReferences()){
			visit(viewRef);
		}
	}
	
	public void visit(final IViewReference viewRef){
		Control control = null;
		try {
			Object pane = getPaneMethod.invoke(viewRef);
			control = (Control) getControlMethod.invoke(pane);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		if (control != null) {
			dispatch(control);
		}else{
			// NOP
		}
	}
}
