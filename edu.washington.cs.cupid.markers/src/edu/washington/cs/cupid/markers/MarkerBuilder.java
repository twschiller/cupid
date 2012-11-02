package edu.washington.cs.cupid.markers;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Maps;

public class MarkerBuilder implements IMarkerBuilder{

	private final Map<String,Object> attributes = Maps.newHashMap();
	
	private final IResource resource;
	
	public MarkerBuilder(IResource resource){
		this.resource = resource;
	}
	
	public MarkerBuilder set(String attributeName, boolean value){
		attributes.put(attributeName, value);
		return this;
	}
	
	public MarkerBuilder set(String attributeName, int value){
		attributes.put(attributeName, value);
		return this;
	}
	
	public MarkerBuilder set(String attributeName, Object value){
		attributes.put(attributeName, value);
		return this;
	}
	
	@Override
	public IMarker create(String type) throws CoreException{
		IMarker marker = resource.createMarker(type);
		marker.setAttributes(attributes);
		return marker;
	}
	
}
