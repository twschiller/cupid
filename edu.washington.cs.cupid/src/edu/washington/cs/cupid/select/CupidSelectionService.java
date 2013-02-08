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
package edu.washington.cs.cupid.select;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.internal.NullPartListener;

/**
 * A selection manager to track selections in Eclipse and SWT Widgets.
 * @author Todd Schiller
 */
public final class CupidSelectionService extends NullPartListener {

	// TODO fix broadcaster memory leak when parts are disposed
	// TODO support for list selection
	
	private Class<?> workbenchPartReferenceClazz = null;
	private Class<?> partPaneClazz = null;
	private Method getPaneMethod = null;
	private Method getControlMethod = null;
	
	private final Map<IWorkbenchPart, Broadcaster> broadcasters = Maps.newHashMap();
	
	private final Set<ICupidSelectionListener> listeners = Sets.newIdentityHashSet();
	
	private static CupidSelectionService instance = null;
	
	/**
	 * Returns the singleton selection service instance.
	 * @return the singleton selection service instance.
	 */
	public static CupidSelectionService getInstance() {
		if (instance == null) {
			instance = new CupidSelectionService();
		}
		return instance;
	}
	
	private CupidSelectionService() {
		try {
			workbenchPartReferenceClazz = Class.forName("org.eclipse.ui.internal.WorkbenchPartReference");
			partPaneClazz = Class.forName("org.eclipse.ui.internal.PartPane");
			getPaneMethod = workbenchPartReferenceClazz.getMethod("getPane");
			getControlMethod = partPaneClazz.getMethod("getControl");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public void partActivated(final IWorkbenchPartReference partRef) {
		injectListeners(partRef);
	}

	@Override
	public void partVisible(final IWorkbenchPartReference partRef) {
		injectListeners(partRef);
	}
	
	/**
	 * Adds selection listeners to the {@link Tree}s and {@link Table}s in <code>partRef</code>.
	 * @param partRef the workbench part to listen for selections in
	 */
	public void injectListeners(final IWorkbenchPartReference partRef) {
		Control control = null;
		
		if (partRef instanceof IViewReference) {
			try {
				Object pane = getPaneMethod.invoke(partRef);
				control = (Control) getControlMethod.invoke(pane);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (control != null) {
				IWorkbenchPart part = partRef.getPart(false);
				if (!broadcasters.containsKey(part)) {
					broadcasters.put(part, new Broadcaster(part));
				}
				
				injectListeners(broadcasters.get(part), control);
			}
		}
	}
	
	/**
	 * Adds a selection listener to the selection service.
	 * @param listener the selection listener
	 */
	public static void addListener(final ICupidSelectionListener listener) {
		if (listener == null){
			throw new NullPointerException("Selection listener cannot be null");
		}
		getInstance().listeners.add(listener);
	}
	
	/**
	 * Removes a selection listener from the selection service.
	 * @param listener the selection listener to remove
	 */
	public static void removeListener(final ICupidSelectionListener listener) {
		getInstance().listeners.remove(listener);
	}
	
	private void injectListeners(final Broadcaster broadcast, final Control control) {
		
		if (control instanceof Tree) {
			((Tree) control).addSelectionListener(broadcast);
		} else if (control instanceof Table) {
			((Table) control).addSelectionListener(broadcast);
		}
		
		// recursively add listeners
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				injectListeners(broadcast, child);
			}
		}
	}
	
	private class Broadcaster implements SelectionListener {

		private IWorkbenchPart part;
		private int seen = Integer.MIN_VALUE;
		
		public Broadcaster(final IWorkbenchPart part) {
			this.part = part;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {	
			
			if (e.time == seen){
				return;
			} 
			
			seen = e.time;
			System.out.println("Part: " + Integer.toHexString(part.hashCode()) + " Detail:" + e.stateMask + " Time:" + e.time);
			
			if (e.widget instanceof Table) {
				Table table = (Table) e.widget;
				
				if (table.getSelectionCount() == 1) {
					for (ICupidSelectionListener listener : listeners) {
						listener.selectionChanged(part, table.getSelection()[0].getData());
					}
				} else {
					List<Object> selection = Lists.newArrayList();
					for (TableItem item : table.getSelection()) {
						selection.add(item.getData());
					}
					for (ICupidSelectionListener listener : listeners) {
						listener.selectionChanged(part, selection.toArray());
					}
				}
			} else if (e.widget instanceof Tree) {
				Tree tree = (Tree) e.widget;
				
				if (tree.getSelectionCount() == 1) {
					for (ICupidSelectionListener listener : listeners) {
						listener.selectionChanged(part, tree.getSelection()[0].getData());
					}
				} else {
					List<Object> selection = Lists.newArrayList();
					for (TreeItem item : tree.getSelection()) {
						selection.add(item.getData());
					}
					for (ICupidSelectionListener listener : listeners) {
						listener.selectionChanged(part, selection.toArray());
					}
				}
			}
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
			// NO OP
		}
		
	}

}
