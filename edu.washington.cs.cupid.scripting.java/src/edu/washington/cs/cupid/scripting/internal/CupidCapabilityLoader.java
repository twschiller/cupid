package edu.washington.cs.cupid.scripting.internal;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import com.google.common.collect.Sets;

import edu.washington.cs.cupid.scripting.java.internal.Activator;

/**
 * Custom class loader for Cupid capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class CupidCapabilityLoader extends ClassLoader{
	
	// REFERENCE: http://stackoverflow.com/questions/3971534/how-to-force-java-to-reload-class-upon-instantiation
	
	public CupidCapabilityLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> findClass(String fullyQualifiedName) {
		try{
			byte[] bytes = loadClassData(fullyQualifiedName);
			return defineClass(fullyQualifiedName, bytes, 0, bytes.length);
		}catch(IOException ex){
			return null;
		}
	}
	
	private byte[] loadClassData(File file) throws IOException{
		int size = (int) file.length();
		
		byte buffer[] = new byte[size];
		
		DataInputStream data = new DataInputStream(new FileInputStream(file));
		data.readFully(buffer);
		data.close();
		
		return buffer;
	}
	
	private byte[] loadClassData(String className) throws IOException {
		IProject cupidProject = Activator.getDefault().getCupidProject();
		File file = new File(cupidProject.getFolder("bin").getRawLocation().toFile(), className + ".class");
		return loadClassData(file);
	}
		
	public static class CapabilityResourceLocator implements IResourceVisitor{

		private final Set<ICompilationUnit> classes = Sets.newHashSet();
		
		public Set<ICompilationUnit> getClasses() {
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
	}
}
