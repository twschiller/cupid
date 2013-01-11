package edu.washington.cs.cupid.scripting.java.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

/**
 * Manages the Cupid scripting project for Java
 * @author Todd Schiller
 */
public class JavaProjectManager implements IResourceChangeListener{

	public static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";

	public static void populateCupidProject(IProject project, IProgressMonitor monitor) throws CoreException, IOException{
		// http://www.pushing-pixels.org/2008/11/18/extending-eclipse-creating-a-java-project-without-displaying-a-wizard.html
		
		// create source directory
		IPath srcPath = new Path("src");
		IFolder srcFolder = project.getFolder(srcPath);
		srcFolder.create(true, true, monitor);
		
		// create bin directory
		IPath binPath = new Path(PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.SRCBIN_BINNAME));
		IFolder binFolder = project.getFolder(binPath);
		binFolder.create(IResource.FORCE | IResource.DERIVED, true, monitor);
		binFolder.setDerived(true, monitor);
		
		// refresh directories
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		// load the project 
		IJavaProject javaProject = JavaCore.create(project);

		// Add a Java nature to the project
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = JAVA_NATURE;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	
		List<IClasspathEntry> classpath = Lists.newArrayList();
		
		classpath.add(JavaCore.newSourceEntry(project.getFullPath().append(srcPath)));	
		
		IPath containerPath = new Path(JavaRuntime.JRE_CONTAINER);
		classpath.add(JavaCore.newContainerEntry(containerPath));
		
		String bundles [] = new String[] {
			"org.eclipse.core.runtime",
			"org.eclipse.core.resources",
			"org.eclipse.equinox.common",
			"org.eclipse.core.expressions",
		};
		
		for (String bundle : bundles){
			classpath.add(JavaCore.newLibraryEntry(bundlePath(Platform.getBundle(bundle)), null, null));
		}
		
		
		javaProject.setRawClasspath(classpath.toArray(new IClasspathEntry[]{}), monitor);
		
		// TWS: another possible resource
		// http://www.stateofflow.com/journal/66/creating-java-projects-programmatically
	}
	
	private static IPath bundlePath(Bundle bundle) throws IOException{
		URL url = FileLocator.resolve(bundle.getEntry("/"));
		
		URL foo = FileLocator.toFileURL(url);
		String path = url.getPath();
		
		if (path.startsWith("file:")){
			path = path.substring("file:".length());
		}
		
		if (path.endsWith("!/")){
			path = path.substring(0, path.length() - 2);
		}else{
			path = path + "/bin/";
		}
		
		File file = new File(path);
		
		String absolute = file.getAbsolutePath();
		
		return new Path(absolute);
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new DynamicChangeVisitor());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static class DynamicChangeVisitor implements IResourceDeltaVisitor{
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {

			IResource resource = delta.getResource();
			if (resource != null && resource.getProject() == Activator.getDefault().getCupidProject() && interesting(delta)){
				IJavaElement element = JavaCore.create(resource);

				if (element instanceof IClassFile && !element.getElementName().contains("$")){
					IClassFile file = (IClassFile) element;
					try {
						Activator.getDefault().loadDynamicCapability(file, true);
					} catch (Exception e) {
						Activator.getDefault().logError("Error reloading dynamic capability " + element.getElementName(), e);
					} catch (Error e){
						// caused by unresolved compilation problems
					}
					return false;
				}				
			}

			return true;
		}	
		
		/**
		 * @param delta the delta
		 * @return <code>true</code> iff the delta causes resources to be invalidated
		 */
		private boolean interesting(IResourceDelta delta){
			return (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.TYPE)) != 0;
		}
	}
}
