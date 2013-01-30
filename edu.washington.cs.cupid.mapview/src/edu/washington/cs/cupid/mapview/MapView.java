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
package edu.washington.cs.cupid.mapview;

import java.util.Map;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.mapview.internal.Activator;
import edu.washington.cs.cupid.mapview.internal.GraphLabelProvider;
import edu.washington.cs.cupid.mapview.internal.GraphNodeContentProvider;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.select.ICupidSelectionListener;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;
import edu.washington.cs.cupid.usage.events.EventConstants;

/**
 * A view that displays the output of a capability as a map for the current selection.
 * @author Todd Schiller
 * @see http://www.vogella.com/articles/EclipseZest/article.html
 */
public class MapView extends ViewPart implements IZoomableWorkbenchPart, ICupidSelectionListener {
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.chart.views.HistogramView";

	@SuppressWarnings("rawtypes")
	private ICapability capability;

	@SuppressWarnings("rawtypes")
	private static final TypeToken<Map> ACCEPTED_OUTPUT_TYPE = TypeToken.of(Map.class);
	
	private static final String NAME = "Map View";
	
	private GraphViewer viewer;
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		CupidDataCollector.record(
				new CupidEventBuilder(EventConstants.LOADED_WHAT, getClass(), Activator.getDefault())
				.create());
	}

	@Override
	public final void createPartControl(final Composite parent) {
		viewer = new GraphViewer(parent, SWT.BORDER);
		
		CupidSelectionService.addListener(this);
		
		refreshCapabilities();
		
		CupidPlatform.getCapabilityRegistry().addChangeListener(new ICapabilityChangeListener() {
			@Override
			public void onChange(final ICapabilityPublisher publisher) {
				refreshCapabilities();
			}
		});
		
		setPartName(NAME);
		setContentDescription("Please select a capability.");
	}

	private void buildMap(final Map<?, ?> map) {
		final GraphNodeContentProvider model = new GraphNodeContentProvider(map);
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!viewer.getGraphControl().isDisposed()){
					viewer.setContentProvider(model);
					viewer.setLabelProvider(new GraphLabelProvider());
					viewer.setInput(model.getNodes());
					viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
					viewer.applyLayout();
					
					fillToolBar();
				}	
			};
		});
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		// NO OP
	}

	/**
	 * @see http://www.vogella.com/articles/EclipseZest/article.html
	 */
	private void fillToolBar() {
//	Don't add zoom options to menu, since capabilities are listed there.
//		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
//		IActionBars bars = getViewSite().getActionBars();
//		bars.getMenuManager().add(toolbarZoomContributionViewItem);
	}

	@Override
	public final AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void showMapping(final Object input) {
		MapView.this.showBusy(true);
			
		if (input == null || capability == null) {
			return;
		}
			
		if (input != null && TypeManager.isCompatible(capability, input)) {
			CapabilityExecutor.asyncExec(capability, TypeManager.getCompatible(capability, input), MapView.this, new NullJobListener() {
				@Override
				public void done(final IJobChangeEvent event) {
					CapabilityStatus<?> status = (CapabilityStatus<?>) event.getResult();
					
					if (status.value() != null && status.isOK()) {
						buildMap((Map) status.value());
					}
				
					MapView.this.showBusy(false);
				}
			});
	
		}
	}
	

	/**
	 * Add the list of available capabilities to the view's menu.
	 */
	private void refreshCapabilities() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (MapView.this) {
					//http://wiki.eclipse.org/FAQ_How_do_I_add_actions_to_a_view's_menu_and_toolbar%3F
					final IActionBars actionBars = getViewSite().getActionBars();
					final IMenuManager dropDownMenu = actionBars.getMenuManager();
					
					dropDownMenu.removeAll();
					
					for (final ICapability<?, ?> available : CupidPlatform.getCapabilityRegistry().getCapabilities()) {
						if (TypeManager.isJavaCompatible(ACCEPTED_OUTPUT_TYPE, available.getReturnType())) {
							dropDownMenu.add(new Action(available.getName()) {
								@Override
								public void run() {
									MapView.this.capability = available;
									MapView.this.setPartName(NAME + ": " + available.getName());
									MapView.this.setContentDescription(available.getDescription());
									
									CupidDataCollector.record(
											CupidEventBuilder.selectCapabilityEvent(MapView.this.getClass(), capability, Activator.getDefault())
											.create());	
								}
							});
						}
					}
					
					dropDownMenu.markDirty();
					actionBars.updateActionBars();
				}
			}
		});
	}
	
	@Override
	public final void selectionChanged(final IWorkbenchPart part, final ISelection selection) {	
		CupidDataCollector.record(
				CupidEventBuilder.contextEvent(getClass(), part, selection, Activator.getDefault())
				.create());
		
		if (selection instanceof StructuredSelection) {	
			// TODO handle multiple selected elements
			final StructuredSelection all = ((StructuredSelection) selection);
			showMapping(all.getFirstElement());
		} 
		// TODO handle other selection types
	}
	
	@Override
	public final void selectionChanged(final IWorkbenchPart part, final Object data) {
		CupidDataCollector.record(
				CupidEventBuilder.contextEvent(getClass(), part, data, Activator.getDefault())
				.create());
		
		showMapping(data);
	}

	@Override
	public final void selectionChanged(final IWorkbenchPart part, final Object[] data) {
		CupidDataCollector.record(
				CupidEventBuilder.contextEvent(getClass(), part, data, Activator.getDefault())
				.create());
		
		// TODO handle multiple selected objects
		showMapping(data[0]);
	}

	@Override
	public final void dispose() {
		CupidSelectionService.removeListener(this);
		super.dispose();
	}
}
