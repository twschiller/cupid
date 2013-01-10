package edu.washington.cs.cupid.views;

public class ViewRule {
	private String qualifiedType;
	private String capability;
	private boolean active;
	
	public ViewRule(String qualifiedType, String capability, boolean active) {
		this.qualifiedType = qualifiedType;
		this.capability = capability;
		this.active = active;
	}

	public String getQualifiedType() {
		return qualifiedType;
	}

	public String getCapability() {
		return capability;
	}

	public boolean isActive() {
		return active;
	}
}
