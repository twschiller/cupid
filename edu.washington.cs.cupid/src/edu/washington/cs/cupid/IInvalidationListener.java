package edu.washington.cs.cupid;

import java.util.Set;

import org.eclipse.core.resources.IResourceChangeEvent;

/**
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface IInvalidationListener {
	
	/**
	 * Callback when a resource change causes invalidation
	 * @param invalidated the objects evicted from the cache
	 * @param event the original resource change event
	 */
	void onResourceChange(Set<Object> invalidated, IResourceChangeEvent event);
}
