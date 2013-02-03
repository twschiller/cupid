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
package edu.washington.cs.cupid.preferences;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.washington.cs.cupid.capability.options.IConfigurableCapability;
import edu.washington.cs.cupid.capability.options.Option;
import edu.washington.cs.cupid.internal.CupidActivator;

/**
 * Serialize capability preferences using JSON.
 * @author Todd Schiller
 */
public class CapabilityPreferenceStore {

	private final Gson gson;
	private final IPreferenceStore store;

	private Map<String, Map<String, String>> values;
	
	/**
	 * Construct a preference store to serialize capability preferences using JSON.
	 */
	public CapabilityPreferenceStore(){
		gson = new Gson();
		store = CupidActivator.getDefault().getPreferenceStore();
		String json = store.getString(PreferenceConstants.P_CAPABILITY_OPTIONS);
		values = gson.fromJson(json, new TypeToken<Map<String, Map<String, String>>>(){}.getType());
	}
	
	/**
	 * Returns the user preference for the given option and capability, or {@link Option#getDefault()}
	 * if no user preference is set.
	 * @param capability the capability
	 * @param option the option
	 * @return the user preference for the given option and capability, or {@link Option#getDefault()}
	 */
	public <T> T get(final IConfigurableCapability<?,?> capability, final Option<T> option){
		if (values.containsKey(capability.getUniqueId())){
			Map<String, String> forCapability = values.get(capability.getUniqueId());
			if (forCapability.containsKey(option.getName())){
				String json = values.get(capability.getUniqueId()).get(option.getName());
				return gson.fromJson(json, option.getType().getType()) ;	
			} else {
				return option.getDefault();
			}
			
		} else {
			return option.getDefault();
		}
	}
	
	/**
	 * Returns the user preferences for <code>capability</code>.
	 * @param capability the capability
	 * @return the user preferences for <code>capability</code>.
	 */
	public Map<Option<?>, Object> get(final IConfigurableCapability<?,?> capability) {
		Map<Option<?>, Object> result = Maps.newHashMap();
		for (Option<?> option : capability.getOptions()){
			result.put(option, get(capability, option));
		}
		return result;
	}
	
	/**
	 * Sets the user preference to <code>value</code> for the given capability option.
	 * @param capability the capability
	 * @param option the option
	 * @param value the user preference
	 */
	public <T> void set(final IConfigurableCapability<?,?> capability, final Option<T> option, T value){
		if (!values.containsKey(capability.getUniqueId())){
			values.put(capability.getUniqueId(), Maps.<String,String>newHashMap());
		}
		
		String json = gson.toJson(value, option.getType().getType());
		values.get(capability.getUniqueId()).put(option.getName(), json);
		
		store();
	}
	
	private void store(){
		String json = gson.toJson(values, new TypeToken<Map<String, Map<String, String>>>(){}.getType());
		store.setValue(PreferenceConstants.P_CAPABILITY_OPTIONS, json);
	}
}
