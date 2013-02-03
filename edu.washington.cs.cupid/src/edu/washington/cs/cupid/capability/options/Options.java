package edu.washington.cs.cupid.capability.options;

import java.util.Map;

import com.google.common.collect.Maps;

public class Options {

	private final Map<Option<?>, Object> map;
	private final int hash;
	
	public static final Options DEFAULT = new Options();
	
	private Options(){
		this.map = Maps.newLinkedHashMap();
		this.hash = 0;
	}
	
	public <T> Options add(Option<T> option, T value){
		Options result = new Options();
		result.map.putAll(this.map);
		result.map.put(option, value);
		result.calculateHash();
		return result;
	}

	public <T> boolean hasValue(Option<T> option){
		return map.containsKey(option);
	}
	
	public <T> Object get(Option<T> option){
		return map.get(option);
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	private int calculateHash(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Options other = (Options) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
}
