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
package edu.washington.cs.cupid.views;

/**
 * Rule for specifying toString capabilities (projections).
 * @author Todd Schiller
 */
public final class ViewRule {
	private String qualifiedType;
	private String capability;
	private boolean active;
	
	/**
	 * Construct a rule for generating a {@link String} representation of a type.
	 * @param qualifiedType the type the rule applies to
	 * @param capability the id of the string generating capability
	 * @param active <code>true</code> iff the rule is active
	 */
	public ViewRule(final String qualifiedType, final String capability, final boolean active) {
		this.qualifiedType = qualifiedType;
		this.capability = capability;
		this.active = active;
	}

	/**
	 * Returns the qualified type name that the rule applied to.
	 * @return the qualified type name that the rule applied to.
	 */
	public String getQualifiedType() {
		return qualifiedType;
	}

	/**
	 * Returns the capability id used to produce a {@link String} output.
	 * @return the capability id used to produce a {@link String} output
	 */
	public String getCapability() {
		return capability;
	}

	/**
	 * Returns <code>true</code> iff the rule is active.
	 * @return <code>true</code> iff the rule is active.
	 */
	public boolean isActive() {
		return active;
	}
}
