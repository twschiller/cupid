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
	public FormattingRule(final String name) {
		setName(name);
	}
	
	/**
	 * Create a new formatting rule named <code>name</code>.
	 * @param name the name of the formatting rule
	 * @param capabilityId the associated capability, or <code>null</code>
	 * @param format the format
	 * @param active <code>true</code> iff the rule is active
	 */
	public FormattingRule(final String name, final String capabilityId, final Format format, final boolean active) {
		this(name);
		this.capabilityId = capabilityId;
		this.active = active;
	}
	
	/**
	 * Returns a <i>shallow</i> copy of the formatting rule. 
	 * @return a <i>shallow</i> copy of the formatting rule
	 */
	public final FormattingRule copy() {
		return new FormattingRule(this.name, this.capabilityId, this.format, this.active);
	}

	
	/**
	 * @return the name of the formatting rule
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name of the formatting rule
	 */
	public final void setName(final String name) {
		checkNotNull(name, "formatting rule name cannot be null");
		this.name = name;
	}

	/**
	 * @return the format
	 */
	public final Format getFormat() {
		return format;
	}

	/**
	 * @param format the non-<code>null</code> format
	 */
	public final void setFormat(final Format format) {
		checkNotNull(name, "formatting rule format cannot be null");
		this.format = format;
	}

	/**
	 * @return the associated capability, or <code>null</code>
	 */
	public final String getCapabilityId() {
		return capabilityId;
	}

	/**
	 * @param capabilityId the associated capability, or <code>null</code>
	 */
	public final void setCapabilityId(final String capabilityId) {
		this.capabilityId = capabilityId;
	}

	/**
	 * @return <code>true</code> iff the rule is active
	 */
	public final boolean isActive() {
		return active;
	}

	/**
	 * @param active <code>true</code> iff the rule is active
	 */
	public final void setActive(final boolean active) {
		this.active = active;
	}

}
