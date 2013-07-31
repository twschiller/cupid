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
import org.eclipse.ui.IWorkbenchPartReference;
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
	
	public boolean visit(final Tree tree){
		for (final TreeItem item : tree.getItems()) {
			visit(tree, item);
		}
		return true;
	}
	
	public boolean visit(final Tree tree, final TreeItem item){
		return true;
	}
	
	public boolean visit(final Table table, final TableItem item){
		return true;
	}
	
	public boolean visit(final Composite composite){
		return true;
	}
	
	public boolean visit(final Table table){
		for (final TableItem item : table.getItems()) {
			visit(table, item);
		}
		return true;
	}
	
	public boolean visit(final Control control){
		return true;
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
	
	public boolean visit(final IWorkbenchWindow window){
		for (IWorkbenchPage page: window.getPages()){
			visit(page);
		}
		return true;
	}
	
	public boolean visit(final IWorkbench workbench){
		for (IWorkbenchWindow window: workbench.getWorkbenchWindows()){
			visit(window);
		}
		return true;
	}
	
	public boolean visit(final IWorkbenchPage page){
		visit(page.getActivePartReference());
		return true;
	}
	
	public boolean visit(final IWorkbenchPartReference partRef) {
		Control control = null;
		
		if (partRef instanceof IViewReference) {
			try {
				Object pane = getPaneMethod.invoke(partRef);
				control = (Control) getControlMethod.invoke(pane);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (control != null) {
				dispatch(control);
			}
		}
		
		return true;
	}
}
