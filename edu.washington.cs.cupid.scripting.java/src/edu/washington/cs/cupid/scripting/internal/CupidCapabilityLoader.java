package edu.washington.cs.cupid.scripting.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;

import com.google.common.io.Files;

import edu.washington.cs.cupid.scripting.java.internal.Activator;

/**
 * Custom class loader for Cupid capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class CupidCapabilityLoader extends ClassLoader{
	
	// REFERENCE: http://stackoverflow.com/questions/3971534/how-to-force-java-to-reload-class-upon-instantiation
	
	// TODO this is going to break for packages in the Cupid project
	
	public CupidCapabilityLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> findClass(String fullyQualifiedName) {
		try{
			byte[] bytes = readCupidClass(fullyQualifiedName);
			return defineClass(fullyQualifiedName, bytes, 0, bytes.length);
		}catch(IOException ex){
			throw new RuntimeException(new ClassNotFoundException(fullyQualifiedName, ex));
		}
	}
		
	private byte[] readCupidClass(String fullyQualifiedName) throws IOException {
		IProject cupidProject = Activator.getDefault().getCupidProject();
		File file = new File(cupidProject.getFolder("bin").getRawLocation().toFile(), fullyQualifiedName + ".class");
		return Files.toByteArray(file);
	}
}
