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
package edu.washington.cs.cupid.jdt.types;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.types.ITypeAdapter;

public class JavaResourceAdapter implements ITypeAdapter<IJavaElement, IResource> {

	@Override
	public TypeToken<IJavaElement> getInputType() {
		return TypeToken.of(IJavaElement.class);
	}

	@Override
	public TypeToken<IResource> getOutputType() {
		return TypeToken.of(IResource.class);
	}

	@Override
	public IResource adapt(IJavaElement input){
		try {
			return input.getCorrespondingResource();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
}
