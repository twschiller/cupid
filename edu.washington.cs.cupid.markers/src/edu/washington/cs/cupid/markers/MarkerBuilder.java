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
package edu.washington.cs.cupid.markers;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Maps;

/**
 * A marker builder with a basic fluent interface.
 * @author Todd Schiller
 */
public class MarkerBuilder implements IMarkerBuilder {

	private final Map<String, Object> attributes = Maps.newHashMap();
	
	private final IResource resource;
	
	/**
	 * Construct a marker builder for <code>resource</code> without any attributes set.
	 * @param resource the resource
	 */
	public MarkerBuilder(final IResource resource) {
		this.resource = resource;
	}
	
	/**
	 * Sets the boolean-valued attribute with the given name.
	 * @param attributeName the name of the attribute
	 * @param value the value
	 * @return the marker builder
	 * @see {@link IMarker#set(String, boolean)}
	 */
	public final MarkerBuilder set(final String attributeName, final boolean value) {
		attributes.put(attributeName, value);
		return this;
	}
	
	/**
	 * Sets the integer-valued attribute with the given name.
	 * @param attributeName the name of the attribute
	 * @param value the value
	 * @return the marker builder
	 * @see {@link IMarker#set(String, int)}
	 */
	public final MarkerBuilder set(final String attributeName, final int value) {
		attributes.put(attributeName, value);
		return this;
	}
	
	/**
	 * Sets the obkect-valued attribute with the given name.
	 * @param attributeName the name of the attribute
	 * @param value the value
	 * @return the marker builder
	 * @see {@link IMarker#set(String, Object)}
	 */
	public final MarkerBuilder set(final String attributeName, final Object value) {
		attributes.put(attributeName, value);
		return this;
	}
	
	@Override
	public final IMarker create(final String type) throws CoreException {
		IMarker marker = resource.createMarker(type);
		marker.setAttributes(attributes);
		return marker;
	}
	
}
