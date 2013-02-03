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
package edu.washington.cs.cupid.capability.options;

import java.util.List;
import java.util.Map;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.ICapability;

/**
 * Interface for capabilities that provide options.
 * @author Todd Schiller
 */
public interface IConfigurableCapability<I, T> extends ICapability<I, T> {
	
	/**
	 * Returns the options for the capability.
	 * @return the options for the capability.
	 */
	List<Option<?>> getOptions();	
	
	/**
	 * Returns the option with name <code>name</code>.
	 * @param name the option name
	 * @return the option with name <code>name</code>
	 * @throws IllegalArgumentException if <code>name</code> is not the name of an option
	 */
	Option<?> getOption(String name) throws IllegalArgumentException;
	
	/**
	 * Returns the a job that calculates a result for <code>input</code> with <code>options</code>
	 * when executed.
	 * @param input the execution's input
	 * @param options the execution's options
	 * @see CapabilityJob
	 * @return a job that calculates a result for <code>input</code> when executed
	 */
	ConfigurableCapabilityJob<I, T> getJob(I input, Options options);
	
	/**
	 * Returns the a job that calculates a result for <code>input</code> with the default options.
	 * @param input the execution's input
	 * @see CapabilityJob
	 * @return a job that calculates a result for <code>input</code> when executed
	 */
	@Override
	ConfigurableCapabilityJob<I, T> getJob(I input);
}
