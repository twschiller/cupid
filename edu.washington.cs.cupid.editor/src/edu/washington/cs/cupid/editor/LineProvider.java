package edu.washington.cs.cupid.editor;

import org.eclipse.swt.graphics.RGB;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IOutput;

public class LineProvider {
	
	private final ICapability capability;
	private final ICapability.IOutput<?> output;
	private final RGB color;
	
	public LineProvider(ICapability capability, IOutput<?> output, RGB/*?*/ color) {
		this.capability = capability;
		this.output = output;
		this.color = color;
	}

	public ICapability getCapability() {
		return capability;
	}

	public ICapability.IOutput<?> getOutput() {
		return output;
	}

	public RGB getColor() {
		return color;
	}
}
