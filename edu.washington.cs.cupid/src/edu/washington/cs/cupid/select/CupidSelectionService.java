package edu.washington.cs.cupid.select;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.washington.cs.cupid.internal.NullPartListener;

/**
 * A selection manager to track selections in Eclipse and SWT Widgets
 * @author Todd Schiller
 */
public class CupidSelectionService extends NullPartListener implements ISelectionListener{

	// TODO fix broadcaster memory leak when parts are disposed
	// TODO support for list selection
	
	private Class<?> workbenchPartReferenceClazz = null;
	private Class<?> partPaneClazz = null;
	private Method getPaneMethod = null;
	private Method getControlMethod = null;

	private final Map<IWorkbenchPart, Broadcaster> broadcasters = Maps.newHashMap();
	
	private final List<ICupidSelectionListener> listeners = Lists.newArrayList();
	
	private static CupidSelectionService instance = null;
	
	public static CupidSelectionService getInstance(){
		if (instance == null){
			instance = new CupidSelectionService();
		}
		return instance;
	}
	
	private CupidSelectionService(){
		try{
			workbenchPartReferenceClazz = Class.forName("org.eclipse.ui.internal.WorkbenchPartReference");
			partPaneClazz = Class.forName("org.eclipse.ui.internal.PartPane");
			getPaneMethod = workbenchPartReferenceClazz.getMethod("getPane");
			getControlMethod = partPaneClazz.getMethod("getControl");
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		injectListeners(partRef);
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		injectListeners(partRef);
	}
	
	public void injectListeners(IWorkbenchPartReference partRef){
		Control control = null;
		
		if (partRef instanceof IViewReference){
			try {
				Object pane = getPaneMethod.invoke(partRef);
				control = (Control) getControlMethod.invoke(pane);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (control != null){
				IWorkbenchPart part = partRef.getPart(false);
				if (!broadcasters.containsKey(part)){
					broadcasters.put(part, new Broadcaster(part));
				}
				
				injectListeners(broadcasters.get(part), control);
			}
		}
	}
	
	public static void addListener(ICupidSelectionListener listener){
		getInstance().listeners.add(listener);
	}
	
	public static void removeListener(ICupidSelectionListener listener){
		getInstance().listeners.remove(listener);
	}
	
	private void injectListeners(Broadcaster broadcast, Control control){
		
		if (control instanceof Tree){
			((Tree) control).addSelectionListener(broadcast);
		}else if(control instanceof Table){
			((Table) control).addSelectionListener(broadcast);
		}
		
		// recursively add listeners
		if (control instanceof Composite){
			for (Control child : ((Composite) control).getChildren()){
				injectListeners(broadcast, child);
			}
		}
	}
	
	private class Broadcaster implements SelectionListener{

		private IWorkbenchPart part;
		
		public Broadcaster(IWorkbenchPart part) {
			this.part = part;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.widget instanceof Table){
				Table table = (Table) e.widget;
				
				if (table.getSelectionCount() == 1){
					for (ICupidSelectionListener listener : listeners){
						listener.selectionChanged(part, table.getSelection()[0].getData());
					}
				}else{
					List<Object> selection = Lists.newArrayList();
					for (TableItem item : table.getSelection()){
						selection.add(item.getData());
					}
					for (ICupidSelectionListener listener : listeners){
						listener.selectionChanged(part, selection.toArray());
					}
				}
			}else if (e.widget instanceof Tree){
				Tree tree = (Tree) e.widget;
				
				if (tree.getSelectionCount() == 1){
					for (ICupidSelectionListener listener : listeners){
						listener.selectionChanged(part, tree.getSelection()[0].getData());
					}
				}else{
					List<Object> selection = Lists.newArrayList();
					for (TreeItem item : tree.getSelection()){
						selection.add(item.getData());
					}
					for (ICupidSelectionListener listener : listeners){
						listener.selectionChanged(part, selection.toArray());
					}
				}
			}else{
				// TODO log this?
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// NO OP
		}
		
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		for (ICupidSelectionListener listener : listeners){
			listener.selectionChanged(part, selection);
		}
	}
	
}
