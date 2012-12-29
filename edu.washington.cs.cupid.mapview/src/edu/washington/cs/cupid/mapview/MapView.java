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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.mapview.internal.GraphLabelProvider;
import edu.washington.cs.cupid.mapview.internal.GraphNodeContentProvider;

/**
 * A view that displays the output of a capability as a map for the current selection.
 * @author Todd Schiller
 * @see http://www.vogella.com/articles/EclipseZest/article.html
 */
public class MapView extends ViewPart implements IZoomableWorkbenchPart, ISelectionListener{
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.chart.views.HistogramView";

	@SuppressWarnings("rawtypes")
	private ICapability capability;

	/**
	 * Capability outputs accepted
	 */
	@SuppressWarnings("rawtypes")
	private static final TypeToken<Map> ACCEPT = TypeToken.of(Map.class);
	
	private static final String NAME = "Map View";
	
	private GraphViewer viewer;
	
	private ISelectionService selectionService;
	
	public void createPartControl(Composite parent) {
		viewer = new GraphViewer(parent, SWT.BORDER);
		
		selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.addPostSelectionListener(this);
		
		refreshCapabilities();
		
		CupidPlatform.getCapabilityRegistry().addChangeListener(new ICapabilityChangeListener(){
			@Override
			public void onChange(ICapabilityPublisher publisher) {
				refreshCapabilities();
			}
		});
		
		setPartName(NAME);
		setContentDescription("Please select a capability.");
	}

	private void buildMap(Map<?,?> map){
		final GraphNodeContentProvider model = new GraphNodeContentProvider(map);
		
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				
				viewer.setContentProvider(model);
				viewer.setLabelProvider(new GraphLabelProvider());
				viewer.setInput(model.getNodes());
				viewer.setLayoutAlgorithm( new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
				viewer.applyLayout();
				
				fillToolBar();
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
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(toolbarZoomContributionViewItem);
	}

	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {	
		if (capability != null && selection instanceof StructuredSelection){
			MapView.this.showBusy(true);
			
			final StructuredSelection all = ((StructuredSelection) selection);
			
			final Object input = all.getFirstElement();
			
			if (CapabilityExecutor.isCompatible(capability, input)){
				CapabilityExecutor.asyncExec(capability, CapabilityExecutor.getCompatible(capability, input), MapView.this, new NullJobListener(){
					@Override
					public void done(IJobChangeEvent event) {
						CapabilityStatus<?> status = (CapabilityStatus<?>) event.getResult();
						
						if (status.value() != null && status.isOK()){
							buildMap((Map)status.value());
						}
					
						MapView.this.showBusy(false);
					}
				});
		
			}
		}
	}
	
	
	
	/**
	 * Add the list of available capabilities to the view's menu
	 */
	private void refreshCapabilities(){
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				synchronized(MapView.this){
					//http://wiki.eclipse.org/FAQ_How_do_I_add_actions_to_a_view's_menu_and_toolbar%3F
					final IActionBars actionBars = getViewSite().getActionBars();
					final IMenuManager dropDownMenu = actionBars.getMenuManager();
					
					dropDownMenu.removeAll();
					
					for (final ICapability<?,?> capability : CupidPlatform.getCapabilityRegistry().getCapabilities()){
						if (CapabilityExecutor.isResultCompatible(capability, ACCEPT)){
							dropDownMenu.add(new Action(capability.getName()){
								@Override
								public void run() {
									MapView.this.capability = capability;
									MapView.this.setPartName(NAME + ": " + capability.getName());
									MapView.this.setContentDescription(capability.getDescription());
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


}