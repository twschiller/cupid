package edu.washington.cs.cupid.chart.internal;

import java.awt.Frame;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IParameter;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.select.ICupidSelectionListener;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;
import edu.washington.cs.cupid.usage.events.EventConstants;

public abstract class ChartViewPart extends ViewPart implements ICupidSelectionListener {

	protected ICapability capability;
	protected ICapability.IOutput<?> output;
	protected Method outputMethod;
	protected Method outputElementMethod;
	
	protected boolean showListResults = false;
	
	protected List<Object> comboModel = Lists.newArrayList();
	
	protected ConcurrentMap<Object, Object> results;
	
	protected Frame frame;
	
	private Composite cSelectOutput;
	private Combo cOutput;
	
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
		
		parent.setLayout(new GridLayout());
		
		Composite inner = new Composite(parent, SWT.NONE);
		inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		inner.setLayout(new GridLayout());
		
		cSelectOutput = new Composite(inner, SWT.NONE);
		GridLayout lSelectOutput = new GridLayout();
		lSelectOutput.numColumns = 2;
		cSelectOutput.setLayout(lSelectOutput);
		
		Label lOutput = new Label(cSelectOutput, SWT.LEFT);
		lOutput.setText("Select Output:");
		cOutput = new Combo(cSelectOutput, SWT.DROP_DOWN | SWT.READ_ONLY);
		cOutput.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	
		cOutput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (capability != null){
					try{
						Object selected = comboModel.get(cOutput.getSelectionIndex());
						
						if (selected instanceof ICapability.IOutput){
							output = (ICapability.IOutput<?>) selected;
							outputMethod = null;
						}else if (selected instanceof Method){
							output = CapabilityUtil.singleOutput(capability);
							
							if (showListResults){
								outputElementMethod = (Method) selected;
								outputMethod = null;
							}else{
								outputElementMethod = null;
								outputMethod = (Method) selected;
							}
						}else{
							throw new RuntimeException("Unexpected output model entry of type " + selected.getClass().getName());
						}
					}catch(Exception ex){
						// NO OP
					}
				}
			}
		});
		
		cSelectOutput.setVisible(false);
		
		Composite cFrame = new Composite(inner, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		cFrame.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		frame = SWT_AWT.new_Frame(cFrame);
			
		parent.pack();
		
		setPartName(getName());
		setContentDescription("No capability provided.");
	}
	
	private static boolean producesList(ICapability capability){
		if (CapabilityUtil.hasSingleOutput(capability)){
			TypeToken<?> outType = CapabilityUtil.singleOutput(capability).getType();
			return outType.getType() instanceof ParameterizedType &&
				   List.class.isAssignableFrom(outType.getRawType());
		}else{
			return false;
		}
	}
	
	private static Type elementType(ICapability capability){
		TypeToken<?> outType = CapabilityUtil.singleOutput(capability).getType();
		return ((ParameterizedType) outType.getType()).getActualTypeArguments()[0];
	}
	
	
	public void setCapability(ICapability capability) throws IllegalArgumentException{

		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				frame.removeAll();
			}
		});
		
		if (CapabilityUtil.isUnary(capability)){
			
			List<ICapability.IOutput<?>> compatible = Lists.newArrayList();
			List<Method> compatibleMethods = Lists.newArrayList();
			List<Method> compatibleElementMethods = Lists.newArrayList();
			
			for (ICapability.IOutput<?> output : capability.getOutputs()){	
				for (TypeToken<?> type : accepts()){
					if (TypeManager.isJavaCompatible(type, output.getType())){
						compatible.add(output);
					}
				}
			}
			
			if (producesList(capability)){	
				Class<?> eltType = (Class<?>) elementType(capability);
				for (Method m : eltType.getMethods()){
					if (m.getParameterTypes().length == 0 && m.getName().startsWith("get")){
						for (TypeToken<?> type : accepts()){
							if (TypeManager.isJavaCompatible(type, TypeManager.boxType(TypeToken.of(m.getReturnType())))){
								compatibleElementMethods.add(m);
							}
						}	
					}
				}
			} else if (CapabilityUtil.hasSingleOutput(capability)){
				ICapability.IOutput<?> output = CapabilityUtil.singleOutput(capability);
				
				Type t = output.getType().getType();
				if (t instanceof Class){
					for (Method m : ((Class<?>) t).getMethods()){
						if (m.getParameterTypes().length == 0 && m.getName().startsWith("get")){
							for (TypeToken<?> type : accepts()){
								if (TypeManager.isJavaCompatible(type, TypeManager.boxType(TypeToken.of(m.getReturnType())))){
									compatibleMethods.add(m);
								}
							}	
						}
					}
				}
			}
			
			if (compatible.isEmpty() && compatibleMethods.isEmpty() && compatibleElementMethods.isEmpty()){
				throw new IllegalArgumentException("Capability '" + capability.getName() + "' has no compatible outputs");
			}
			
			this.capability = capability;
			this.output = !compatible.isEmpty() ? compatible.get(0) : null;	
			this.outputElementMethod = compatible.isEmpty() && !compatibleElementMethods.isEmpty() ? compatibleElementMethods.get(0) : null;
			this.outputMethod = compatible.isEmpty() && compatibleElementMethods.isEmpty() ? compatibleMethods.get(0) : null;
			
			showListResults = !compatibleElementMethods.isEmpty();
			
			if (compatible.size() + compatibleMethods.size() + compatibleElementMethods.size() <= 1){
				this.cSelectOutput.setVisible(false);
			}else{
				this.cSelectOutput.setVisible(true);
			}
			
			this.cOutput.removeAll();
			this.comboModel.clear();
			
			for (ICapability.IOutput<?> o : compatible){
				this.cOutput.add(o.getName());
				this.comboModel.add(o);
			}
			
			if (!showListResults){
				
				for (Method m : compatibleMethods){
					this.cOutput.add(m.getName());
					this.comboModel.add(m);
				}
			}else{
				for (Method m : compatibleElementMethods){
					this.cOutput.add(m.getName());
					this.comboModel.add(m);
				}
			}
			
			this.cOutput.select(0);
			
			this.setPartName(getName() + ": " + capability.getName());
			this.setContentDescription(capability.getDescription());
			
		}else{
			throw new IllegalArgumentException("Capability " + capability.getName() + " is not single input");
		}
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
									? status.value().getOutput(output)
									: status.getException();
						
						if (outputElementMethod != null){
							List<?> resultList = (List<?>) result;
							List<Object> transformed = Lists.newArrayList();
							for (Object elt : resultList){
								try {
									transformed.add(outputElementMethod.invoke(elt));
								} catch (IllegalArgumentException e) {
									throw new RuntimeException("Incompatible output of type " + result.getClass() + " for method " + outputMethod.getName());
								} catch (Exception e) {
									result = e;
									break;
								}
							}
							result = transformed;
						}else if (outputMethod != null){
							try {
								result = outputMethod.invoke(result);
							} catch (IllegalArgumentException e) {
								throw new RuntimeException("Incompatible output of type " + result.getClass() + " for method " + outputMethod.getName());
							} catch (Exception e) {
								result = e;
							}
						}
								
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
