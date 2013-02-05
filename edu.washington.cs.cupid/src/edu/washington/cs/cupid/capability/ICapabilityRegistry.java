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
package edu.washington.cs.cupid.capability;

import java.util.SortedSet;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;


/**
 * The capability registry holds the master list of all available capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface ICapabilityRegistry extends ICapabilityPublisher, ICapabilityChangeListener {

	/**
	 * Returns an unmodifiable view of the available capabilities.
	 * @return an unmodifiable view of the available capabilities
	 */
	SortedSet<ICapability> getCapabilities();

	/**
	 * Returns the available capabilities that are compatible with input of the given type.
	 * @param type the query
	 * @return the capabilities that are compatible with input of the given type
	 */
	SortedSet<ICapability> getCapabilities(TypeToken<?> type);

	/**
	 * Returns the available capabilities that are compatible with the given types.
	 * @param outputType the output type
	 * @return the capabilities that are compatible with output of the given type
	 */
	SortedSet<ICapability> getCapabilitiesForOutput(TypeToken<?> outputType);
	
	/**
	 * Returns the capabilities that output boolean values.
	 * @return the capabilities that output boolean values
	 */
	SortedSet<ICapability> getPredicates();
	
	/**
	 * Returns the available capabilities that are compatible with the given types.
	 * @param inputType the input type
	 * @param outputType the output type
	 * @return the capabilities that are compatible with input of the given type
	 */
	SortedSet<ICapability> getCapabilities(TypeToken<?> inputType, TypeToken<?> outputType);

	/**
	 * Returns the capability with the given unique id.
	 * @param uniqueId the capability's unique id
	 * @return the capability with the given unique id
	 * @throws NoSuchCapabilityException iff a capability with the given id is not available
	 */
	ICapability findCapability(String uniqueId) throws NoSuchCapabilityException;

	/**
	 * Returns the viewer associated with <code>type</code>, or 
	 * <code>null</code> if no viewer is associated.
	 * @param type the output type
	 * @return the viewer associated with <code>type</code>, or <code>null</code>
	 */
	ICapability getViewer(TypeToken<?> type);
	
	/**
	 * Register a capability that is not provided via an {@link ICapabilityPublisher}, e.g.
	 * by an extension point.
	 * @param capability the capability
	 */
	void registerStaticCapability(ICapability capability);
	
	/**
	 * Register a capability publisher.
	 * @param publisher the capability publisher
	 */
	void registerPublisher(ICapabilityPublisher publisher);
}
