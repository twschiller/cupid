package edu.washington.cs.cupid.usage.events;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.collect.Maps;

import edu.washington.cs.cupid.capability.ICapability;

public class CupidEventBuilder {

	public static final String CREATE_CAPABILITY = "CREATE";
	
	private AbstractUIPlugin plugin;
	private String what;
	private String kind;
	private Map<String, String> data = Maps.newHashMap();
	
	public CupidEventBuilder(String what, Class<?> clazz, AbstractUIPlugin plugin){
		this.plugin = plugin;
		this.what = what;
		this.kind = clazz.getSimpleName();
	}
	
	public CupidEventBuilder(String what, String kind, AbstractUIPlugin plugin){
		this.plugin = plugin;
		this.what = what;
		this.kind = kind;
	}
	
	public CupidEvent create(){
		return new CupidEvent(
				what, kind, data, 
				plugin.getBundle().getSymbolicName(), plugin.getBundle().getVersion().toString(), System.currentTimeMillis());
	}
	
	public CupidEventBuilder addData(String key, String value){
		data.put(key, value);
		return this;
	}
	
	public static CupidEvent createCapabilityEvent(String source, ICapability<?,?> capability, AbstractUIPlugin plugin){
		Map<String, String> data = Maps.newHashMap();
		
		data.put("name", capability.getName());
		data.put("parameterType", capability.getParameterType().toString());
		data.put("returnType", capability.getReturnType().toString());
		data.put("pure", Boolean.toString(capability.isPure()));
		data.put("transient", Boolean.toString(capability.isTransient()));
		
		return new CupidEvent(
				CREATE_CAPABILITY,
				source, 
				data,
				plugin.getBundle().getSymbolicName(),
				plugin.getBundle().getVersion().toString(),
				System.currentTimeMillis());
		
	}
	
}
