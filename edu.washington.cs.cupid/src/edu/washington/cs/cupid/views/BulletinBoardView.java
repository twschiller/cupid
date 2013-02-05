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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.capability.linear.ILinearCapability;
import edu.washington.cs.cupid.utility.CapabilityUtil;

/**
 * A view that lists available capabilities in a table.
 * @author Todd Schiller
 */
public final class BulletinBoardView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.views.BulletinBoardView";

	private TableViewer viewer;
	
	class ViewContentProvider implements IStructuredContentProvider, ICapabilityChangeListener {
		private ICapabilityPublisher publisher;
		
		@Override
		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
			// NO OP
		}
		
		public ViewContentProvider(final ICapabilityPublisher publisher) {
			super();
			this.publisher = publisher;
			this.publisher.addChangeListener(this);
		}

		@Override
		public void dispose() {
			publisher.removeChangeListener(this);
		}
		
		@Override
		public Object[] getElements(final Object parent) {
			return publisher.publish();	
		}

		@Override
		public void onChange(final ICapabilityPublisher notifier) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!viewer.getTable().isDisposed()) {
						viewer.setInput(getViewSite());
					}
				}
			});
		}
	}
	
	/**
	 * The constructor.
	 */
	public BulletinBoardView() {		
	}
	
	private TableViewerColumn createColumn(final String title, final int bound, final int colNumber) {
	    final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
	    final TableColumn column = viewerColumn.getColumn();
	    column.setText(title);
	    column.setWidth(bound);
	    column.setResizable(true);
	    column.setMoveable(false);
	    return viewerColumn;
	}
	
	@Override
	public void createPartControl(final Composite parent) {	
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider(CupidPlatform.getCapabilityRegistry()));
		
		final TableViewerColumn nameColumn = createColumn("Name", 100, 0);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((ICapability) element).getName();
			}
		});
		
		final TableViewerColumn descriptionColumn = createColumn("Description", 200, 1);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((ICapability) element).getDescription();
			}
		});
		
		final TableViewerColumn inputColumn = createColumn("Input", 100, 2);
		inputColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ILinearCapability){
					return TypeManager.simpleTypeName(((ILinearCapability<?,?>) element).getParameter().getType().getType());
				} else {
					return null;
				}
			}
		});
		
		final TableViewerColumn outputColumn = createColumn("Output", 100, 2);
		outputColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ILinearCapability){
					return TypeManager.simpleTypeName(((ILinearCapability<?,?>) element).getOutput().getType().getType());
				} else {
					return null;
				}
			}
		});
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(4));
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(1));
		viewer.getTable().setLayout(layout);
		
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setInput(getViewSite());
		
		// sort table by capability name
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer context, final Object lhs, final Object rhs) {
				return CapabilityUtil.COMPARE_NAME.compare((ICapability) lhs, (ICapability) rhs);
			}
		});
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
