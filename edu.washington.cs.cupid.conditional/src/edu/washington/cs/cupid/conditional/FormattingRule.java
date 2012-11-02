package edu.washington.cs.cupid.conditional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A conditional capability-based formatting rule.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class FormattingRule {

	private String name;
	private String capabilityId = null;
	private Format format = new Format();
	private boolean active;
	
	/**
	 * Create a formatting rule with named <code>name</code>.
	 * @param name the name of the formatting rule
	 */
	public FormattingRule(String name){
		setName(name);
	}
	
	/**
	 * Create a new formatting rule named <code>name</code>.
	 * @param name the name of the formatting rule
	 * @param capabilityId the associated capability, or <code>null</code>
	 * @param foreground the foreground override, or <code>null</code>
	 * @param background the background override, or <code>null</code>
	 * @param font the font override
	 * @param active <code>true</code> iff the rule is active
	 */
	public FormattingRule(String name, String capabilityId, Format format, boolean active) {
		this(name);
		this.capabilityId = capabilityId;
		this.active = active;
	}
	
	/**
	 * Returns a copy of the formatting rule. Note: foreground, background, and font are shallow copies.
	 * @return a copy of the formatting rule
	 */
	public FormattingRule copy(){
		return new FormattingRule(this.name, this.capabilityId, this.format, this.active);
	}

	
	/**
	 * @return the name of the formatting rule
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name of the formatting rule
	 */
	public void setName(String name) {
		checkNotNull(name, "formatting rule name cannot be null");
		this.name = name;
	}

	/**
	 * @return the format
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * @param format the non-<code>null</code> format
	 */
	public void setFormat(Format format) {
		checkNotNull(name, "formatting rule format cannot be null");
		this.format = format;
	}

	/**
	 * @return the associated capability, or <code>null</code>
	 */
	public String getCapabilityId() {
		return capabilityId;
	}

	/**
	 * @param capabilityId the associated capability, or <code>null</code>
	 */
	public void setCapabilityId(String capabilityId) {
		this.capabilityId = capabilityId;
	}

	/**
	 * @return <code>true</code> iff the rule is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active <code>true</code> iff the rule is active
	 */
	public void setActive(boolean active) {
		this.active = active;
	} 
}
