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
package edu.washington.cs.cupid.markers;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import com.google.common.reflect.TypeToken;

/**
 * Interface for a 
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface IMarkerBuilder {

	public static final TypeToken<Collection<IMarkerBuilder>> MARKER_RESULT = new TypeToken<Collection<IMarkerBuilder>>(){
		private static final long serialVersionUID = 1L;
	};
	
	/**
	 * Returns the marker; the new marker has the specified type.
	 * @param type a type string, specifying its type (e.g. "<tt>org.eclipse.core.resources.taskmarker</tt>")
	 * @return the marker; the new marker has the specified type.
	 * @throws CoreException if an error occurs when building the marker
	 * @see IMarker
	 */
	public IMarker create(String type) throws CoreException;
}
