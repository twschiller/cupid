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
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import edu.washington.cs.cupid.CupidPlatform;

/**
 * Utility methods for working with the workspace and project classpaths.
 * @author Todd Schiller
 */
public final class ClasspathUtil {
	
	private ClasspathUtil() {
		// NO OP
	}
	
	/**
	 * Returns the bundle providing class with <code>qualifiedName</code>.
	 * @param qualifiedName the qualified name of the class query
	 * @return the bundle providing class with <code>qualifiedName</code>
	 * @throws ClassNotFoundException if the class is not found
	 */
	public static Bundle bundleForClass(final String qualifiedName) throws ClassNotFoundException {
		// Use CupidPlatform because of its buddy-policy
		Class<?> clazz = Class.forName(qualifiedName, false, CupidPlatform.class.getClassLoader());
		return FrameworkUtil.getBundle(clazz);
	}
	
	/**
	 * Returns the JAR of <code>className</code> used by <code>bundle</code>.
	 * @param bundle
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static File jarForClass(final Bundle bundle, final String className) throws ClassNotFoundException, IOException {
		Class<?> clazz = bundle.loadClass(className);
		CodeSource src = clazz.getProtectionDomain().getCodeSource();
		URL location = src.getLocation();
		String resolved = FileLocator.resolve(location).getPath();
		
		if (resolved.toUpperCase().endsWith(".JAR")) {
			return new File(resolved);
		} else {
			throw new IOException("Class is not located within a JAR file. Path:" + resolved);
		}
	}
	
	/**
	 * Returns the {@link URL} for <code>jar</code> provided by <code>bundle</code>.
	 * @param bundle the containing bundle
	 * @param jar the name of the jar, including the <code>.jar</code> extension
	 * @return the {@link URL} for <code>jar</code> provided by <code>bundle</code>.
	 * @throws IOException if a JAR URL cannot be resolved
	 */
	public static URL urlForJar(final Bundle bundle, final String jar) throws IOException {
		Enumeration<URL> jars = bundle.findEntries("/", jar, true);
		
		while (jars.hasMoreElements()) {
			URL next = jars.nextElement();
			URL resolved = FileLocator.resolve(next);
			if (jars.hasMoreElements()) {
				throw new RuntimeException("Multiple URLs for JAR");
			}
			return resolved;
		}
		
		throw new IllegalArgumentException("JAR " + jar + " not in bundle " + bundle.getSymbolicName());
	
		// Can use this if the plug-in is unzipped?
//		Enumeration<URL> paths = testBundle.getResources(qq);
//		while (paths.hasMoreElements()){
//			URL jar = paths.nextElement();
//			URL resolved = FileLocator.resolve(jar);
//			System.out.println("hello");
//		}
	}
	
	/**
	 * Returns the {@link IPath} to <code>bundle</code> using {@link #urlToPath(URL)}.
	 * @param bundle the bundle
	 * @return the {@link IPath} to <code>bundle</code>
	 * @throws IOException if bundles URL cannot be resolved
	 */
	public static IPath bundlePath(final Bundle bundle) throws IOException {
		if (bundle == null) {
			throw new NullPointerException("Bundle cannot be null");
		}
		
		URL url = FileLocator.resolve(bundle.getEntry("/"));
		return urlToPath(url);
	}

	/**
	 * Returns the {@link Path} for <code>url</code>. Assumes that URL is a file-system URL (starts with <code>file:/</code>)
	 * or is already a path. If <code>url</code> refers to a file inside a JAR, the path to the containing JAR is returned; if
	 * URL refers to a directory, the <code>bin</code> subdirectory is returned.
	 * @param url the URL
	 * @return the {@link Path} for <code>url</code>
	 */
	public static Path urlToPath(final URL url) {
		String path = url.getPath();
		
		if (path.startsWith("file:")) {
			path = path.substring("file:".length());
		}
		
		if (path.endsWith("!/")) {
			path = path.substring(0, path.length() - 2);
		}
		
		File file = new File(path);
		
		// TODO should this be a different function?
		if (file.isDirectory()) {
			file = new File(file, "bin");
		}
		
		String absolute = file.getAbsolutePath();
		
		return new Path(absolute);
	}
}
	
