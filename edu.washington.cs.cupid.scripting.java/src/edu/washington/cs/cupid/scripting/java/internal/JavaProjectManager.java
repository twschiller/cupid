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
package edu.washington.cs.cupid.scripting.java.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
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
 * Manages the Cupid scripting project for Java.
 * @author Todd Schiller
 */
public final class JavaProjectManager implements IResourceChangeListener {

	private static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";

	private static final String [] ECLIPSE_BUNDLES = new String[] {
			"org.eclipse.core.runtime",
			"org.eclipse.core.resources",
			"org.eclipse.equinox.common",
			"org.eclipse.core.expressions",
			"org.eclipse.core.jobs",
	};
	
	/**
	 * Setup the Cupid script project. Creates source directory, bin directory, and constructs the
	 * classpath.
	 * @param project the project
	 * @param monitor a progress monitor
	 * @throws CoreException if project creation fails
	 * @throws IOException if a file system error occurs
	 */
	public static void populateCupidProject(final IProject project, final IProgressMonitor monitor) throws CoreException, IOException {
		// http://www.pushing-pixels.org/2008/11/18/extending-eclipse-creating-a-java-project-without-displaying-a-wizard.html
		// http://www.stateofflow.com/journal/66/creating-java-projects-programmatically
	
		final int totalWork = 5;
		
		monitor.beginTask("Populate Cupid Project", totalWork);
		
		// create source directory
		IPath srcPath = new Path("src");
		IFolder srcFolder = project.getFolder(srcPath);
		srcFolder.create(true, true, new SubProgressMonitor(monitor, 1));
		
		// create bin directory
		IPath binPath = new Path(PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.SRCBIN_BINNAME));
		IFolder binFolder = project.getFolder(binPath);
		binFolder.create(IResource.FORCE | IResource.DERIVED, true, new SubProgressMonitor(monitor, 1));
		binFolder.setDerived(true, new SubProgressMonitor(monitor, 1));
		
		// refresh directories
		project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
		
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
		
		for (String symbolicName : ECLIPSE_BUNDLES) {
			Bundle bundle = Platform.getBundle(symbolicName);
			
			if (bundle == null) {
				throw new RuntimeException("Cannot locate bundle " + symbolicName);
			}
			
			classpath.add(JavaCore.newLibraryEntry(bundlePath(bundle), null, null));
		}
		
		// Add Google Guava
		CodeSource guavaSrc = Lists.class.getProtectionDomain().getCodeSource();
		classpath.add(JavaCore.newLibraryEntry(urlToPath(guavaSrc.getLocation()), null, null));
			
		Bundle cupid = Platform.getBundle("edu.washington.cs.cupid");
		
		classpath.add(JavaCore.newLibraryEntry(bundlePath(cupid), null, null));
		
		javaProject.setRawClasspath(classpath.toArray(new IClasspathEntry[]{}), new SubProgressMonitor(monitor, 1));
			
		monitor.done();
	}
	
	private static Path urlToPath(final URL url) {
		String path = url.getPath();
		
		if (path.startsWith("file:")) {
			path = path.substring("file:".length());
		}
		
		if (path.endsWith("!/")) {
			path = path.substring(0, path.length() - 2);
		}
		
		File file = new File(path);
		
		if (file.isDirectory()) {
			file = new File(file, "bin");
		}
		
		String absolute = file.getAbsolutePath();
		
		return new Path(absolute);
	}
	
	public static IPath bundlePath(final Bundle bundle) throws IOException {
		if (bundle == null) {
			throw new NullPointerException("Bundle cannot be null");
		}
		
		URL url = FileLocator.resolve(bundle.getEntry("/"));
		return urlToPath(url);
	}
	
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new DynamicChangeVisitor());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static class DynamicChangeVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(final IResourceDelta delta) throws CoreException {

			IResource resource = delta.getResource();
			if (resource != null && resource.getProject() == Activator.getDefault().getCupidProject() && interesting(delta)) {
				IJavaElement element = JavaCore.create(resource);

				if (element instanceof IClassFile && !element.getElementName().contains("$")) {
					IClassFile file = (IClassFile) element;
					try {
						Activator.getDefault().loadDynamicCapability(file, true);
					} catch (Exception e) {
						Activator.getDefault().logError("Error reloading dynamic capability " + element.getElementName(), e);
					} catch (Error e) {
						// caused by unresolved compilation problems
					}
					return false;
				}				
			}

			return true;
		}	
		
		/**
		 * Return <code>true</code> iff the delta causes resources to be invalidated.
		 * @param delta the delta
		 * @return <code>true</code> iff the delta causes resources to be invalidated
		 */
		private boolean interesting(final IResourceDelta delta) {
			return (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.TYPE)) != 0;
		}
	}
}
