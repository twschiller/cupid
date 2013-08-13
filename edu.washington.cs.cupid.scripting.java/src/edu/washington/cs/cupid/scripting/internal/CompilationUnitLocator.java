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
package edu.washington.cs.cupid.scripting.internal;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import com.google.common.collect.Sets;

/**
 * Visitor that collects the {@link ICompilationUnit}s for a given resource. Ignores
 * inner classes.
 * @author Todd Schiller
 */
public final class CompilationUnitLocator implements IResourceVisitor {
	private final Set<ICompilationUnit> classes = Sets.newHashSet();
	
	@Override
	public boolean visit(final IResource resource) throws CoreException {
		IJavaElement element = JavaCore.create(resource);
		
		if (element instanceof ICompilationUnit) {
			ICompilationUnit cu = (ICompilationUnit) element;
			
			// exclude inner and hidden classes
			if (!element.getElementName().contains("$") && !cu.getResource().isHidden()) {
				classes.add(cu);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the compilation units found by the locator.
	 * @return the compilation units found by the locator
	 */
	public Set<ICompilationUnit> getCapabilityClasses() {
		return classes;
	}
	
	/**
	 * Returns the compilation units associated with <code>resource</code>.
	 * @param resource the query resource
	 * @return the compilation units associated with <code>resource</code>.
	 * @throws CoreException if the locate fails
	 */
	public static Set<ICompilationUnit> locate(final IResource resource) throws CoreException {
		CompilationUnitLocator locator = new CompilationUnitLocator();
		resource.accept(locator);
		return locator.getCapabilityClasses();
	}
}
