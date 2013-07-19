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

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

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

	private static final Image ICON_OPTION = getImage("bullet_wrench.png");
	private static final Image ICON_INPUT = getImage("bullet_blue.png");
	private static final Image ICON_OUTPUT = getImage("bullet_orange.png");
	  
	private Composite panel;
	private Text search;
	private TreeViewer viewer;
	private ViewContentProvider provider;
	
	private class ViewContentProvider implements ITreeContentProvider, ICapabilityChangeListener {
		private ICapabilityPublisher publisher;
		
		public ViewContentProvider(final ICapabilityPublisher publisher) {
			super();
			this.publisher = publisher;
			this.publisher.addChangeListener(this);
		}
		
		@Override
		public void onChange(final ICapabilityPublisher notifier) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!viewer.getTree().isDisposed()) {
						viewer.setInput(getViewSite());
					}
				}
			});
		}

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
			return publisher.publish();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ICapability){
				ICapability c = (ICapability) parentElement;
			
				if (CapabilityUtil.isLinear(c) && CapabilityUtil.options(c).isEmpty()){
					return new Object[] {};
				}
				
				List<Object> result = Lists.newArrayList();
				
				for (ICapability.IParameter<?> p : c.getParameters()){
					result.add(p);
				}
				for (ICapability.IOutput<?> o : c.getOutputs()){
					result.add(o);
				}
					
				return result.toArray();
			}else{
				return new Object[] {};
			}
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ICapability){
				ICapability c = (ICapability) element;
				return !CapabilityUtil.isLinear(c) || !CapabilityUtil.options(c).isEmpty();
			}else{
				return false;
			}
		}
	}
	
	/**
	 * Helper method to load images
	 */
	 private static Image getImage(String file) {
	    Bundle bundle = FrameworkUtil.getBundle(BulletinBoardView.class);
	    URL url = FileLocator.find(bundle, new Path("icons/" + file), null);
	    ImageDescriptor image = ImageDescriptor.createFromURL(url);
	    return image.createImage();
	 } 
		
	/**
	 * The constructor.
	 */
	public BulletinBoardView() {		
	}
	
	private TreeViewerColumn createColumn(final String title, final int bound, final int colNumber) {
		final TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.LEFT);
	    final TreeColumn column = viewerColumn.getColumn();
	    column.setText(title);
	    column.setWidth(bound);
	    column.setResizable(true);
	    column.setMoveable(false);
	    return viewerColumn;
	}
	
	
	private boolean isMatch(String query, String text){
		return text.toUpperCase().contains(query.toUpperCase());
	}
	
	@Override
	public void createPartControl(final Composite parent) {	
		
		provider = new ViewContentProvider(CupidPlatform.getCapabilityRegistry());
		
		panel = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		panel.setLayout(gridLayout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		search = new Text(panel, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		search.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		search.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				viewer.setFilters(new ViewerFilter[]{
						new ViewerFilter(){
							@Override
							public boolean select(Viewer viewer,
									Object parentElement, Object element) {
								
								if (element instanceof ICapability){
									final String query = search.getText();
									ICapability capability = (ICapability) element;
									
									return query.isEmpty() 
									      || isMatch(query, capability.getName()) 
									      || isMatch(query, capability.getDescription());	
								}else{
									return true;
								}
							}
						}
				});
			}
		});
		
		viewer = new TreeViewer(panel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(provider);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final TreeViewerColumn nameColumn = createColumn("Name", 100, 0);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ICapability){
					return ((ICapability) element).getName();
				}else if (element instanceof ICapability.IParameter){
					return ((ICapability.IParameter<?>) element).getName();
				}else if (element instanceof ICapability.IOutput){
					return ((ICapability.IOutput<?>) element).getName();
				}else{
					throw new RuntimeException("Unexpected tree entry of type " + element.getClass());
				}
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof ICapability){
					return null;
				}else if (element instanceof ICapability.IParameter){
					return ((ICapability.IParameter<?>) element).hasDefault() ? ICON_OPTION : ICON_INPUT;
				}else if (element instanceof ICapability.IOutput){
					return ICON_OUTPUT;
				}else{
					throw new RuntimeException("Unexpected tree entry of type " + element.getClass());
				}
			}
		});
		
		final TreeViewerColumn descriptionColumn = createColumn("Description", 200, 1);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ICapability){
					return ((ICapability) element).getDescription();
				}else if (element instanceof ICapability.IParameter){
					return null;
				}else if (element instanceof ICapability.IOutput){
					return null;
				}else{
					throw new RuntimeException("Unexpected tree entry of type " + element.getClass());
				}
			}
		});
		
		final TreeViewerColumn inputColumn = createColumn("Input", 100, 2);
		inputColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ICapability){
					ICapability c = (ICapability) element;
					
					if (CapabilityUtil.isUnary(c)){
						return TypeManager.simpleTypeName(CapabilityUtil.unaryParameter(c).getType());
					}else{
						return null;
					}
				}else if (element instanceof ICapability.IParameter){
					return TypeManager.simpleTypeName(((ICapability.IParameter<?>) element).getType());
				}else if (element instanceof ICapability.IOutput){
					return null;
				}else{
					throw new RuntimeException("Unexpected tree entry of type " + element.getClass());
				}
			}
		});
		
		final TreeViewerColumn outputColumn = createColumn("Output", 100, 2);
		outputColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ICapability){
					ICapability c = (ICapability) element;
					
					if (CapabilityUtil.hasSingleOutput(c)){
						return TypeManager.simpleTypeName((CapabilityUtil.singleOutput(c).getType()));
					}else{
						return null;
					}
				}else if (element instanceof ICapability.IParameter){
					return null;
				}else if (element instanceof ICapability.IOutput){
					return TypeManager.simpleTypeName(((ICapability.IOutput<?>) element).getType());
				}else{
					throw new RuntimeException("Unexpected tree entry of type " + element.getClass());
				}
			}
		});
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(4));
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(1));
		viewer.getTree().setLayout(layout);
		
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		viewer.setInput(getViewSite());
				
		// sort table by capability name
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer context, final Object lhs, final Object rhs) {
				if (lhs instanceof ICapability && rhs instanceof ICapability){
					return CapabilityUtil.COMPARE_NAME.compare((ICapability) lhs, (ICapability) rhs);		
				}else{
					return 0;
				}
			}
		});
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewer.getTree().setMenu(menuManager.createContextMenu(viewer.getTree()));
		
		getSite().registerContextMenu(menuManager, viewer);
		getSite().setSelectionProvider(viewer);
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
