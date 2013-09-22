package edu.washington.cs.cupid.editor.preferences;

import org.eclipse.swt.graphics.RGB;

public class RulerPreference {

	public String capability;
	public String output;
	public RGB color;
	public boolean enabled;
	
	public RulerPreference(){
		// NOP
	}
	
	public RulerPreference(String capability, String output, RGB color, boolean enabled) {
		super();
		this.capability = capability;
		this.output = output;
		this.color = color;
		this.enabled = false;
	}
	
}
