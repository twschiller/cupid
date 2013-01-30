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
package edu.washington.cs.cupid.usage.events;

import java.util.Map;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.collect.Maps;

import edu.washington.cs.cupid.capability.ICapability;

/**
 * Fluent interface for building {@link CupidEvent} for recording usage data.
 * @author Todd Schiller
 */
public final class CupidEventBuilder {

	private Plugin plugin;
	private String what;
	private String kind;
	private Map<String, String> data = Maps.newHashMap();
	
	/**
	 * Construct a fluent interface for building {@link CupidEvent}s.
	 * @param what the type of event
	 * @param clazz the source of the event
	 * @param plugin the plug-in associated with the event
	 */
	public CupidEventBuilder(String what, Class<?> clazz, Plugin plugin){
		this.plugin = plugin;
		this.what = what;
		this.kind = clazz.getName();
	}
	
	/**
	 * Construct a fluent interface for building {@link CupidEvent}s.
	 * @param what the type of event
	 * @param kind the source of the event
	 * @param plugin the plug-in associated with the event
	 */
	public CupidEventBuilder(String what, String kind, Plugin plugin){
		this.plugin = plugin;
		this.what = what;
		this.kind = kind;
	}
	
	/**
	 * Build the {@link CupidEvent}.
	 * @return the {@link CupidEvent}
	 */
	public CupidEvent create(){
		return new CupidEvent(
				what, kind, data, 
				plugin.getBundle().getSymbolicName(), plugin.getBundle().getVersion().toString(), System.currentTimeMillis());
	}
	
	/**
	 * Add a key-value pair to the event.
	 * @param key the key
	 * @param value the value
	 * @return returns the event builder instance
	 */
	public CupidEventBuilder addData(String key, String value){
		data.put(key, value);
		return this;
	}
	
	/**
	 * Add a key-value pairs to the event.
	 * @param key the key
	 * @param value the value
	 * @return returns the event builder instance
	 */
	public CupidEventBuilder addData(Map<String, String> data){
		this.data.putAll(data);
		return this;
	}
	
	public static CupidEventBuilder createAction(final IAction action, final IActionDelegate delegate, final Plugin plugin){
		return new CupidEventBuilder(EventConstants.ACTION_WHAT, action.getId(), plugin).addData("delegate", delegate.getClass().getName());
	}
	
	private static Map<String, String> capabilityData(ICapability<?,?> capability){
		Map<String, String> data = Maps.newHashMap();

		data.put("name", capability.getName());
		data.put("id", capability.getUniqueId());
		data.put("parameterType", capability.getParameterType().toString());
		data.put("returnType", capability.getReturnType().toString());
		data.put("class", capability.getClass().getName());
		data.put("pure", Boolean.toString(capability.isPure()));
		data.put("transient", Boolean.toString(capability.isTransient()));
		data.put("local", Boolean.toString(capability.isLocal()));
		data.put("dynamic", Boolean.toString(!capability.getDynamicDependencies().isEmpty()));

		return data;
	}
	
	public static CupidEventBuilder contextEvent(final Class<?> view, IWorkbenchPart source, Object [] selection, final Plugin plugin){
		Map<String, String> data = Maps.newHashMap();
		
		if (selection == null){
			data.put("empty", Boolean.toString(true));
			data.put("dataClass", null);
		}else{
			data.put("empty", Boolean.toString(selection.length == 0));
			data.put("size", Integer.toString(selection.length));
			data.put("dataClass", selection.getClass().getName());
		}
		
		data.put("sourceClass", source.getClass().getName());
		data.put("sourceTitle", source.getTitle());
		
		return new CupidEventBuilder(EventConstants.SELECTION_CONTEXT_WHAT, view, plugin).addData(data);
	}
	
	public static CupidEventBuilder contextEvent(final Class<?> view, IWorkbenchPart source, Object selection, final Plugin plugin){
		Map<String, String> data = Maps.newHashMap();
		
		data.put("size", Integer.toString(1));
			
		if (selection == null){
			data.put("dataClass", null);
		}else{
			data.put("dataClass", selection.getClass().getName());
		}
		
		data.put("sourceClass", source.getClass().getName());
		data.put("sourceTitle", source.getTitle());
		
		return new CupidEventBuilder(EventConstants.SELECTION_CONTEXT_WHAT, view, plugin).addData(data);
	}
	
	public static CupidEventBuilder contextEvent(final Class<?> view, IWorkbenchPart source, ISelection selection, final Plugin plugin){
		Map<String, String> data = Maps.newHashMap();
		
		data.put("empty", Boolean.toString(selection.isEmpty()));
		data.put("selectionClass", selection.getClass().getName());
		
		if (!selection.isEmpty() && selection instanceof StructuredSelection){
			StructuredSelection structured = ((StructuredSelection) selection);
			data.put("size", Integer.toString(structured.size()));
			
			if (structured.getFirstElement() == null){
				data.put("dataClass", null);
			}else{
				data.put("dataClass", structured.getFirstElement().getClass().getName());
			}
		}
		
		data.put("sourceClass", source.getClass().getName());
		data.put("sourceTitle", source.getTitle());
		
		return new CupidEventBuilder(EventConstants.SELECTION_CONTEXT_WHAT, view, plugin).addData(data);
	}
	
	public static CupidEventBuilder selectCapabilityEvent(final Class<?> source, final ICapability<?,?> capability, final Plugin plugin){
		return new CupidEventBuilder(EventConstants.ACTIVATE_CAPABILITY_WHAT, source, plugin).addData(capabilityData(capability));
	}
	
	public static CupidEventBuilder createCapabilityEvent(final Class<?> source, final ICapability<?,?> capability, final Plugin plugin){
		return new CupidEventBuilder(EventConstants.CREATE_CAPABILITY_WHAT, source, plugin).addData(capabilityData(capability));
	}
	
}
