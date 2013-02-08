package edu.washington.cs.cupid.chart.internal;

import java.awt.Frame;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IParameter;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.select.ICupidSelectionListener;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;
import edu.washington.cs.cupid.usage.events.EventConstants;

public abstract class ChartViewPart extends ViewPart implements ICupidSelectionListener {

	protected ICapability capability;
	
	protected ConcurrentMap<Object, Object> results;
	
	protected Frame frame;
	
	protected abstract void buildChart();
	
	protected abstract String getName();
	
	protected abstract Set<TypeToken<?>> accepts();
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		CupidDataCollector.record(
				new CupidEventBuilder(EventConstants.LOADED_WHAT, getClass(), Activator.getDefault())
				.create());
	}

	@Override
	public void createPartControl(Composite parent) {
		CupidSelectionService.addListener(this);
		
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
					
					for (final ICapability capability : CupidPlatform.getCapabilityRegistry().getCapabilities()){
						if (CapabilityUtil.isLinear(capability)){
							TypeToken<?> returnType = CapabilityUtil.singleOutput(capability).getType();
							
							for (TypeToken<?> type : accepts()){
								if (TypeManager.isJavaCompatible(type, returnType)){
									dropDownMenu.add(new Action(capability.getName()){
										@Override
										public void run() {
											ChartViewPart.this.capability = capability;
											ChartViewPart.this.setPartName(getName() + ": " + capability.getName());
											ChartViewPart.this.setContentDescription(capability.getDescription());
										
											CupidDataCollector.record(
													CupidEventBuilder.selectCapabilityEvent(ChartViewPart.this.getClass(), capability, Activator.getDefault())
													.create());
										}
									});
									break;
								}
							}	
						}
					}
					
					dropDownMenu.markDirty();
					actionBars.updateActionBars();
				}
			}
		});
	}
	
	private void show(Object [] all){
		if (capability != null){
			ChartViewPart.this.showBusy(true);
			
			IParameter<?> parameter = CapabilityUtil.unaryParameter(capability);
			
			final List<Object> compatible = Lists.newArrayList();
			for (Object x : all){
				if (TypeManager.isCompatible(parameter, x)){
					compatible.add(x);
				}
			}
			
			results = Maps.newConcurrentMap();
			
			for (final Object x : compatible){
				ICapabilityArguments packed = CapabilityUtil.packUnaryInput(capability, TypeManager.getCompatible(parameter, x));
				
				CapabilityExecutor.asyncExec(capability, packed, ChartViewPart.this, new NullJobListener(){
					@Override
					public void done(IJobChangeEvent event) {
						CapabilityStatus status = (CapabilityStatus) event.getResult();
						
						Object result = status.value() != null 
								? CapabilityUtil.singleOutputValue(capability, status) 
								: status.getException();
						
						results.put(x, result);
						
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
	public final void selectionChanged(final IWorkbenchPart part, final Object data) {
		CupidDataCollector.record(
				CupidEventBuilder.contextEvent(getClass(), part, data, Activator.getDefault())
				.create());
		
		show(new Object[]{ data });
	}

	@Override
	public final void selectionChanged(final IWorkbenchPart part, final Object[] data) {
		CupidDataCollector.record(
				CupidEventBuilder.contextEvent(getClass(), part, data, Activator.getDefault())
				.create());
		
		show(data);
	}
	
	@Override
	public void setFocus() {
		// NO OP
	}
}
