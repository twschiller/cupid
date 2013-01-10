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
 * Visitor that collects the {@link ICompilationUnit}s for a given resource.
 * @author Todd Schiller
 */
public class CompilationUnitLocator implements IResourceVisitor{
	private final Set<ICompilationUnit> classes = Sets.newHashSet();
	
	public Set<ICompilationUnit> getCapabilityClasses() {
		return classes;
	}
	
	@Override
	public boolean visit(IResource resource) throws CoreException {
		IJavaElement element = JavaCore.create(resource);
		
		if (element instanceof ICompilationUnit){
			if (!element.getElementName().contains("$")){
				classes.add((ICompilationUnit) element);
				return false;
			}
		}
		return true;
	}
	
	public static Set<ICompilationUnit> locate(IResource resource) throws CoreException{
		CompilationUnitLocator locator = new CompilationUnitLocator();
		resource.accept(locator);
		return locator.getCapabilityClasses();
	}
}
