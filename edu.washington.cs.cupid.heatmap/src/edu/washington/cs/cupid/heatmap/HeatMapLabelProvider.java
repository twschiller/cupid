package edu.washington.cs.cupid.heatmap;

import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ICapability;

@SuppressWarnings("restriction")
public class HeatMapLabelProvider extends DecoratingJavaLabelProvider{
	
	@SuppressWarnings("rawtypes")
	private final ICapability capability;
	
	private static Color[] COLORS = new Color[]{
		new Color (Display.getCurrent(), 255, 0, 0),
		new Color (Display.getCurrent(), 0, 255, 0),
		new Color (Display.getCurrent(), 0, 0, 255),
	};
	
	public <F, T extends Number> HeatMapLabelProvider(JavaUILabelProvider original, ICapability<F,T> capability) {
		super(original);
		this.capability = capability;
	}
	
	@Override
	public Color getBackground(Object element) {
		if (capability != null && TypeManager.isCompatible(capability, element)){
			try {
				@SuppressWarnings("unchecked")
				Number result = (Number) CapabilityExecutor.exec(capability, element);
				
				if (result.doubleValue() >= 66){
					return COLORS[1];
				}else if (result.doubleValue() <= 33){
					return COLORS[0];
				}else{
					return COLORS[2];
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
				
				return null;
			}
		}else{
			return null;
		}
	}
}
