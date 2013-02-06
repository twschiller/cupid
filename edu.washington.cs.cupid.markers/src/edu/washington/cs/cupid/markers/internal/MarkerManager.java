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
package edu.washington.cs.cupid.markers.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.ICapabilityRegistry;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.markers.IMarkerBuilder;
import edu.washington.cs.cupid.markers.MarkerBuilder;
import edu.washington.cs.cupid.utility.ResourceDeltaVisitor;

/**
 * Manages markers created with Cupid.
 * @author Todd Schiller
 */
public final class MarkerManager {
	
	// TODO manage deleted resources
	
	private final HashMap<IResource, Set<IMarker>> markers = Maps.newHashMap();
	
	private final MarkerListener listener;
	
	/**
	 * Construct the marker manager.
	 */
	public MarkerManager() {
		listener = new MarkerListener();
	}
	
	/**
	 * Start the marker manager by adding it as a workspace resource change listener.
	 */
	public void start() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_BUILD);		
	}
	
	/**
	 * Stop the marker manager.
	 */
	public void stop() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
	}
	
	private void deleteMarkers(final IResource resource) {
		synchronized (markers) {
			if (markers.containsKey(resource)) {
				for (IMarker marker : markers.get(resource)) {
					try {
						marker.delete();
					} catch (CoreException e) {
						// NO OP
					}
				}
				markers.remove(resource);
			}
		}
	}
	
	private void registerMarker(final IResource resource, final IMarker marker) {
		synchronized (markers) {
			if (!markers.containsKey(resource)) {
				markers.put(resource, Sets.newHashSet(marker));
			} else {
				markers.get(resource).add(marker);
			}
		}
	}
	
	private class MarkerListener implements IResourceChangeListener {
		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(IResourceDelta.CONTENT | IResourceDelta.TYPE);
			try {
				event.getDelta().accept(visitor);
			} catch (CoreException e) {
				return;
			}
	
			ICapabilityRegistry registry = CupidPlatform.getCapabilityRegistry();
			
			for (IResource resource : visitor.getMatches()) {
				deleteMarkers(resource);
				
				SortedSet<ICapability> capabilities = registry.getCapabilities(TypeToken.of(resource.getClass()), IMarkerBuilder.MARKER_RESULT);
				for (ICapability capability : capabilities) {
					// TODO also include generators?
					
					if (CapabilityUtil.isUnary(capability)
						&& TypeManager.isCompatible(CapabilityUtil.unaryParameter(capability), resource)){
								
						Object adapted = TypeManager.getCompatible(CapabilityUtil.unaryParameter(capability), resource);	
						asyncAddMarkers(capability, resource, adapted);
					}
				}
			}
		}

		private void asyncAddMarkers(final ICapability capability, final IResource resource, final Object input) {
			ICapabilityArguments packed = CapabilityUtil.packUnaryInput(capability, input);
			
			CapabilityExecutor.asyncExec(capability, packed, MarkerListener.this, new NullJobListener() {
				@Override
				public void done(final IJobChangeEvent event) {
					CapabilityStatus result = (CapabilityStatus) event.getResult();

					if (result.value() != null) {
						Collection<MarkerBuilder> builders = (Collection<MarkerBuilder>) CapabilityUtil.singleOutputValue(capability, result);
						
						for (MarkerBuilder marker : builders) {
							try {
								registerMarker(resource, marker.create(IMarker.PROBLEM));
							} catch (CoreException e) {
								// NO OP
							}
						}
					} else {
						// TODO report error
					}
				}
			});
		}
		
	}
}
