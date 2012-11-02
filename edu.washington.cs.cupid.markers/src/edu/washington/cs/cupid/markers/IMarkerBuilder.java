package edu.washington.cs.cupid.markers;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import com.google.common.reflect.TypeToken;

/**
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface IMarkerBuilder {

	public static final TypeToken<Collection<IMarkerBuilder>> MARKER_RESULT = new TypeToken<Collection<IMarkerBuilder>>(){
		private static final long serialVersionUID = 1L;
	};
	
	public IMarker create(String type) throws CoreException;
}
