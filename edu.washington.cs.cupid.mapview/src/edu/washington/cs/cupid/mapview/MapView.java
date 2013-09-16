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
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.osgi.framework.CapabilityPermission;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityArguments;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IParameter;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
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
	public static final String ID = "edu.washington.cs.cupid.MapView";

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
	
	public void setCapability(ICapability capability){
		if (CapabilityUtil.isLinear(capability)
				&& TypeManager.isJavaCompatible(ACCEPTED_OUTPUT_TYPE, CapabilityUtil.singleOutput(capability).getType())) {
			
			
			this.capability = capability;
			this.setPartName(NAME + ": " + capability.getName());
			this.setContentDescription(capability.getDescription());
			 
			CupidDataCollector.record(
					CupidEventBuilder.selectCapabilityEvent(this.getClass(), capability, Activator.getDefault())
					.create());
		}else{
			throw new IllegalArgumentException("Capability does not produce a valid mapping: " + capability.getName());
		}
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
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(toolbarZoomContributionViewItem);
	}

	@Override
	public final AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}
	

	private void showMapping(final Object input) {
		MapView.this.showBusy(true);
			
		if (input == null || capability == null) {
			return;
		}
			
		if (CapabilityUtil.isGenerator(capability)){
			ICapabilityArguments empty = new CapabilityArguments();
			
			CapabilityExecutor.asyncExec(capability, empty, MapView.this, new NullJobListener() {
				@Override
				public void done(final IJobChangeEvent event) {
					CapabilityStatus status = (CapabilityStatus) event.getResult();
					
					if (status.value() != null && status.isOK()) {
						buildMap((Map<?, ?>) CapabilityUtil.singleOutputValue(capability, status));
					}
				
					MapView.this.showBusy(false);
				}
			});
			
		}else{
			IParameter<?> parameter = CapabilityUtil.unaryParameter(capability);
			
			if (input != null && TypeManager.isCompatible(parameter, input)) {
				ICapabilityArguments packed = CapabilityUtil.packUnaryInput(capability,  TypeManager.getCompatible(parameter, input));
				
				CapabilityExecutor.asyncExec(capability, packed, MapView.this, new NullJobListener() {
					@Override
					public void done(final IJobChangeEvent event) {
						CapabilityStatus status = (CapabilityStatus) event.getResult();
						
						if (status.value() != null && status.isOK()) {
							buildMap((Map<?, ?>) CapabilityUtil.singleOutputValue(capability, status));
						}
					
						MapView.this.showBusy(false);
					}
				});
			}		
		}
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
