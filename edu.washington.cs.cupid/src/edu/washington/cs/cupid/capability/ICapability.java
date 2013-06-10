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

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.reflect.TypeToken;

/**
 * An Eclipse capability (i.e., service).
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> the input type
 * @param <T> the output type
 */
public interface ICapability {
	
	
	/**
	 * Capability property flags.
	 * @author Todd Schiller
	 */
	enum Flag {
		/**
		 * The capability does not modify state.
		 */
		PURE, 
				
		/**
		 * The capability depends on external state.
		 */
		TRANSIENT
	}
	
	interface IParameter<T> extends Serializable {
		/**
		 * Returns the name of the input.
		 * @return the name of the input
		 */
		String getName();
		
		/**
		 * Returns the type of the input.
		 * @return the type of the input
		 */
		TypeToken<T> getType();
		
		/**
		 * Returns the default value of the input; may be <code>null</code>.
		 * @return the default value of the input
		 */
		T getDefault();
		
		/**
		 * Returns <code>true</code> if the input has a default value
		 * @return <code>true</code> if the input has a default value
		 */
		boolean hasDefault();
	}
	
	/**
	 * The named output of a capability.
	 * @author Todd Schiller
	 * @param <T> the type of the output
	 */
	interface IOutput<T> extends Serializable {
		/**
		 * Returns the name of the output.
		 * @return the name of the output
		 */
		String getName();
		
		/**
		 * Returns the type of the output.
		 * @return the type of the output
		 */
		TypeToken<T> getType();
	}
	
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
	 * @return the capability's parameter types
	 */
	Set<? extends IParameter<?>> getParameters();
	
	/**
	 * @return the capability's return types
	 */
	Set<? extends IOutput<?>> getOutputs();
	
	/**
	 * @param input the execution's input
	 * @see CapabilityJob
	 * @return a job that calculates a result for <code>input</code> when executed
	 */
	CapabilityJob<? extends ICapability> getJob(ICapabilityArguments input);
	
	/**
	 * @return the flags for the capability.
	 */
	EnumSet<Flag> getFlags();
}
