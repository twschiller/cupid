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
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface IMarkerBuilder {

	public static final TypeToken<Collection<IMarkerBuilder>> MARKER_RESULT = new TypeToken<Collection<IMarkerBuilder>>(){
		private static final long serialVersionUID = 1L;
	};
	
	public IMarker create(String type) throws CoreException;
}
