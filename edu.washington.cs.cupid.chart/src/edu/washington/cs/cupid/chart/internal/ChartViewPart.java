package edu.washington.cs.cupid.chart.internal;

import java.awt.Frame;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.jobs.NullJobListener;

public abstract class ChartViewPart extends ViewPart implements ISelectionListener {

	@SuppressWarnings("rawtypes")
	protected ICapability capability;
	
	@SuppressWarnings("rawtypes")
	protected ConcurrentMap results;
	
	protected Frame frame;
	
	private ISelectionService selectionService;
	
	protected abstract void buildChart();
	
	protected abstract String getName();
	
	protected abstract Set<TypeToken<?>> accepts();
	
	@Override
	public void createPartControl(Composite parent) {
		selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.addPostSelectionListener(this);
		
		Composite inner = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		frame = SWT_AWT.new_Frame(inner);
		
		refreshCapabilities();
		
		CupidPlatform.getCapabilityRegistry().addChangeListener(new ICapabilityChangeListener(){
			@Override
			public void onChange(ICapabilityPublisher publisher) {
				refreshCapabilities();
			}
		});
		
		setPartName(getName());
		setContentDescription("Please select a capability.");
	}


	
	/**
	 * Add the list of available capabilities to the view's menu
	 */
	private void refreshCapabilities(){
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				synchronized(ChartViewPart.this){
					//http://wiki.eclipse.org/FAQ_How_do_I_add_actions_to_a_view's_menu_and_toolbar%3F
					final IActionBars actionBars = getViewSite().getActionBars();
					final IMenuManager dropDownMenu = actionBars.getMenuManager();
					
					dropDownMenu.removeAll();
					
					for (final ICapability<?,?> capability : CupidPlatform.getCapabilityRegistry().getCapabilities()){
						for (TypeToken<?> type : accepts()){
							if (TypeManager.isJavaCompatible(type, capability.getReturnType())){
								dropDownMenu.add(new Action(capability.getName()){
									@Override
									public void run() {
										ChartViewPart.this.capability = capability;
										ChartViewPart.this.setPartName(getName() + ": " + capability.getName());
										ChartViewPart.this.setContentDescription(capability.getDescription());
									}
								});
								break;
							}
						}
					}
					
					dropDownMenu.markDirty();
					actionBars.updateActionBars();
				}
			}
		});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {	
		if (capability != null && selection instanceof StructuredSelection){
			ChartViewPart.this.showBusy(true);
			
			final StructuredSelection all = ((StructuredSelection) selection);
			
			final List compatible = Lists.newArrayList();
			for (Object x : all.toList()){
				if (TypeManager.isCompatible(capability, x)){
					compatible.add(x);
				}
			}
			
			results = Maps.newConcurrentMap();
			
			for (final Object x : compatible){
				CapabilityExecutor.asyncExec(capability, TypeManager.getCompatible(capability, x), ChartViewPart.this, new NullJobListener(){
					@Override
					public void done(IJobChangeEvent event) {
						CapabilityStatus<?> status = (CapabilityStatus<?>) event.getResult();
						results.put(x, status.value() != null ? status.value() : status.getException() );
						
						if (results.size() == compatible.size()){
							buildChart();
							ChartViewPart.this.showBusy(false);
						}	
					}
				});
			}
		}
	}
	
	@Override
	public void setFocus() {
		// NO OP
	}
}
