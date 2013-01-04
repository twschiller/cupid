package edu.washington.cs.cupid.views;

import java.util.List;


import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;

public class BulletinBoardView extends ViewPart  {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.views.BulletinBoardView";

	private TableViewer viewer;
	
	class ViewContentProvider implements IStructuredContentProvider, ICapabilityChangeListener {
		private ICapabilityPublisher publisher;
		
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			
		}
		
		public ViewContentProvider(ICapabilityPublisher publisher) {
			super();
			this.publisher = publisher;
			this.publisher.addChangeListener(this);
		}

		@Override
		public void dispose() {
			publisher.removeChangeListener(this);
		}
		
		@Override
		public Object[] getElements(Object parent) {
			return publisher.publish();	
		}

		@Override
		public void onChange(ICapabilityPublisher publisher) {
			Display.getDefault().asyncExec(new Runnable(){
				@Override
				public void run() {
					viewer.setInput(getViewSite());
				}
			});
		}
	}
	
	/**
	 * The constructor.
	 */
	public BulletinBoardView() {		
	}
	
	private TableViewerColumn createColumn(String title, int bound, final int colNumber) {
	    final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
	    final TableColumn column = viewerColumn.getColumn();
	    column.setText(title);
	    column.setWidth(bound);
	    column.setResizable(true);
	    column.setMoveable(false);
	    return viewerColumn;
	}
	
	@Override
	public void createPartControl(Composite parent) {	
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider(CupidPlatform.getCapabilityRegistry()));
		//viewer.setLabelProvider(new ViewLabelProvider());
		
		
		final TableViewerColumn nameColumn = createColumn("Name", 100, 0);
		nameColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				return ((ICapability<?,?>) element).getName();
			}
		});
		
		final TableViewerColumn descriptionColumn = createColumn("Description", 200, 1);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				return ((ICapability<?,?>) element).getDescription();
			}
		});
		
		final TableViewerColumn inputColumn = createColumn("Input", 100, 2);
		inputColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				return TypeManager.simpleTypeName(((ICapability<?,?>) element).getParameterType().getType());
			}
		});
		
		final TableViewerColumn outputColumn = createColumn("Output", 100, 2);
		outputColumn.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				return TypeManager.simpleTypeName(((ICapability<?,?>) element).getReturnType().getType());
			}
		});
		
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setInput(getViewSite());
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}