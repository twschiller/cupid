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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;

/**
 * A view that lists available capabilities in a table.
 * @author Todd Schiller
 */
public final class BulletinBoardView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.views.BulletinBoardView";

	private Composite panel;
	private Text search;
	private TableViewer viewer;
	private ViewContentProvider provider;
	
	private class ViewContentProvider implements IStructuredContentProvider, ICapabilityChangeListener {
		private ICapabilityPublisher publisher;
		private String query = null;
		
		@Override
		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
			// NO OP
		}
		
		public ViewContentProvider(final ICapabilityPublisher publisher) {
			super();
			this.publisher = publisher;
			this.publisher.addChangeListener(this);
		}

		private void refresh(){
			onChange(null);
		}
		
		@Override
		public void dispose() {
			publisher.removeChangeListener(this);
		}
		
		private boolean isMatch(String query, String text){
			return text.toUpperCase().contains(query.toUpperCase());
		}
		
		@Override
		public Object[] getElements(final Object parent) {
			if (query != null){
				List<ICapability> capabilities = Lists.newArrayList();
				for (ICapability capability : publisher.publish()){
					if (isMatch(query, capability.getName()) || isMatch(query, capability.getDescription())){
						// TODO: match of input/output types
						capabilities.add(capability);
					}
				}
				return capabilities.toArray();
			}else{
				return publisher.publish();			
			}
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
		
		provider = new ViewContentProvider(CupidPlatform.getCapabilityRegistry());
		
		panel = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		panel.setLayout(gridLayout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		search = new Text(panel, SWT.SEARCH);
		search.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		search.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				provider.query = search.getText().isEmpty() ? null : search.getText();
				provider.refresh();
			}
		});
		
		viewer = new TableViewer(panel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(provider);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
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
				ICapability capability = (ICapability) element;
				
				if (CapabilityUtil.isUnary(capability)){
					return TypeManager.simpleTypeName(CapabilityUtil.unaryParameter(capability).getType());		
				} else {
					return null;
				}
			}
		});
		
		final TableViewerColumn outputColumn = createColumn("Output", 100, 2);
		outputColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				ICapability capability = (ICapability) element;
				
				if (CapabilityUtil.hasSingleOutput(capability)){
					return TypeManager.simpleTypeName(CapabilityUtil.singleOutput(capability).getType());			
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
