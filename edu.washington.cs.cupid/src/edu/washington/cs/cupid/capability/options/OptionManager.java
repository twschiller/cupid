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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A class to manage options for an {@link IConfigurableCapability}.
 * @author Todd Schiller
 */
public final class OptionManager {

	// TODO implement Map?
	
	private final Map<String, Option<?>> optionMap;
	private final List<Option<?>> options;

	/**
	 * Construct an object manager with the given options.
	 * @param options the options
	 */
	public OptionManager(final Option<?>... options) {
		this.options = Lists.newArrayList();
		this.optionMap = Maps.newHashMap();
		
		for (Option<?> option : options) {
			add(option);
		}
	}
	
	private void add(final Option<?> option) throws IllegalArgumentException {
		String name = option.getName();
		
		if (optionMap.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate option: " + name);
		} else {
			options.add(option);
			optionMap.put(name, option);
		}
	}
	
	/**
	 * Returns the list of options, in the order they were added.
	 * @return the list of options, in the order they were added.
	 */
	public List<Option<?>> getOptions() {
		return Lists.newArrayList(options);
	}
	
	/**
	 * If present, returns the user supplied value for <code>name</code>, otherwise returns
	 * {@link Option#getDefault()}.
	 * @param name the name of the option
	 * @param userSupplied user supplied values
	 * @return the user supplied value for option <code>name</code>, or {@link Optional#getDefault()}
	 * @throws IllegalArgumentException if the option does not exist
	 */
	public Object getValue(final String name, final Options userSupplied) throws IllegalArgumentException {
		if (userSupplied.hasValue(getOption(name))) {
			return userSupplied.get(getOption(name));
		} else if (optionMap.containsKey(name)) {
			Option<?> option = optionMap.get(name);
			return option.getDefault();
		} else {
			throw new IllegalArgumentException("Option does not exist: " + name);	
		}
	}
	
	/**
	 * Returns option <code>name</code>
	 * @param name the name of the option
	 * @return option <code>name</code>
	 * @throws IllegalArgumentException if the option does not exist
	 */
	public Option<?> getOption(final String name) throws IllegalArgumentException {
		if (optionMap.containsKey(name)) {
			return optionMap.get(name);
		} else {
			throw new IllegalArgumentException("Option does not exist: " + name);	
		}
	}
}
