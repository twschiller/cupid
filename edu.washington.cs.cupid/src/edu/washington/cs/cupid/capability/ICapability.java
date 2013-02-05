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

import java.util.Set;

import com.google.common.reflect.TypeToken;

/**
 * An Eclipse capability (i.e., service).
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <T> the output type
 */
public interface ICapability<I, T> {
	
	/**
	 * @return the capability's unique identifier
	 */
	String getUniqueId();
	
	/**
	 * @return a user-friendly name
	 */
	String getName();
	
	/**
	 * @return a short description of the capability
	 */
	String getDescription();

	/**
	 * @return the capability's parameter type
	 */
	TypeToken<I> getParameterType();
	
	/**
	 * @return the capability's return type
	 */
	TypeToken<T> getReturnType();
	
	/**
	 * @param input the execution's input
	 * @see CapabilityJob
	 * @return a job that calculates a result for <code>input</code> when executed
	 */
	CapabilityJob<I, T> getJob(I input);
	
	/**
	 * Returns the unique ids of the capabilities this capability dynamically depends on.
	 * @return the unique ids of the capabilities this capability dynamically depends on
	 */
	Set<String> getDynamicDependencies();
	
	/**
	 * @return <code>true</code> iff the capability does not modify any local or remote state
	 */
	boolean isPure();
	
	/**
	 * <code>true</code> iff the capability works independently on peers.
	 * @return <code>true</code> iff the capability works independently on peers
	 * @deprecated method will be removed in favor of multiple methods
	 */
	boolean isLocal();
	
	/**
	 * Returns <code>true</code> iff the capability depends on remote or transient state. The results
	 * of transient capabilities are not cached.
	 * @return <code>true</code> iff the capability depends on remote or transient state.
	 */
	boolean isTransient();
}
