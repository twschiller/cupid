package edu.washington.cs.cupid.markers.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

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
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityRegistry;
import edu.washington.cs.cupid.jobs.NullJobListener;
import edu.washington.cs.cupid.markers.IMarkerBuilder;
import edu.washington.cs.cupid.markers.MarkerBuilder;
import edu.washington.cs.cupid.utility.ResourceDeltaVisitor;

public class MarkerManager {
	
	// TODO manage deleted resources
	
	private final HashMap<IResource, Set<IMarker>> markers = Maps.newHashMap();
	
	public MarkerManager(){
		System.out.println("Registering marker manager");
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new MarkerListener(), IResourceChangeEvent.POST_BUILD);
	}
	
	private void deleteMarkers(IResource resource){
		synchronized(markers){
			if (markers.containsKey(resource)){
				for (IMarker marker : markers.get(resource)){
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
	
	private void registerMarker(IResource resource, IMarker marker){
		synchronized(markers){
			if (!markers.containsKey(resource)){
				markers.put(resource, Sets.newHashSet(marker));
			}else{
				markers.get(resource).add(marker);
			}
		}
	}
	
	private class MarkerListener implements IResourceChangeListener{
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(IResourceDelta.CONTENT | IResourceDelta.TYPE);
			try {
				event.getDelta().accept(visitor);
			} catch (CoreException e) {
				return;
			}
	
			ICapabilityRegistry registry = CupidPlatform.getCapabilityRegistry();
			for (IResource input : visitor.getMatches()){
				deleteMarkers(input);
				
				for (ICapability<?,?> capability : registry.getCapabilities(TypeToken.of(input.getClass()), IMarkerBuilder.MARKER_RESULT )){
					asyncAddMarkers(capability, input, input);
				}
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void asyncAddMarkers(ICapability capability, final IResource resource, Object input){
			CapabilityExecutor.asyncExec(capability, input, MarkerListener.this, new NullJobListener(){
				@Override
				public void done(IJobChangeEvent event) {
					CapabilityStatus result = (CapabilityStatus) event.getResult();

					for (MarkerBuilder marker : (Collection<MarkerBuilder>) result.value()){
						try {
							registerMarker(resource, marker.create(IMarker.PROBLEM));
						} catch (CoreException e) {
							// NO OP
						}
					}
				}
			});
		}
		
	}
}
