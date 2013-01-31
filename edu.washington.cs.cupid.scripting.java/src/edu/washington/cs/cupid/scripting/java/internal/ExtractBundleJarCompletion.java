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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

/**
 * Quick Fix completion to extract a JAR file from a bundle JAR and add it to the classpath.
 * @author Todd Schiller
 */
public final class ExtractBundleJarCompletion implements IJavaCompletionProposal {

	private static final int RELEVANCE = 100;
	private IJavaProject project;
	private Bundle bundle;
	private String jar;
	
	/**
	 * Construct a Quick Fix completion that extracts a JAR file from a bundle JAR and add it to the classpath.
	 * @param project the Java project
	 * @param bundle the bundle containing the jar
	 * @param the jar file
	 */
	public ExtractBundleJarCompletion(final IJavaProject project, final Bundle bundle, String jar) {
		this.project = project;
		this.bundle = bundle;
		this.jar = jar;
	}
	
	@Override
	public int getRelevance() {
		return RELEVANCE;
	}

	@Override
	public void apply(final IDocument document) {
		try {
			IPath path = ClasspathUtil.bundlePath(bundle);
			JarFile bundleJar = new JarFile(path.toFile());

			JarEntry entry = bundleJar.getJarEntry(jar);			
			if (entry == null){
				throw new RuntimeException("Could not find " + jar + " in JAR for bundle " + bundle.getSymbolicName());
			}
			
			// create the file
			IFolder folder = project.getProject().getFolder("lib");
			IFile file = folder.getFile(jar);
			file.create(bundleJar.getInputStream(entry), true, null);
			
			// update the classpath
			List<IClasspathEntry> cp = Lists.newArrayList(project.getRawClasspath());
			cp.add(JavaCore.newLibraryEntry(file.getFullPath(), null, null));	
			
			project.setRawClasspath(cp.toArray(new IClasspathEntry[]{}), null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Point getSelection(final IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return "Extract " + jar + " from " + this.bundle.getSymbolicName() + " and add to the Cupid project classpath";
	}

	@Override
	public Image getImage() {
		// TODO add quick fix image
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}
	
	private static void extractFile(InputStream inStream, OutputStream outStream) throws IOException {
		byte[] buf = new byte[1024];
		int l;
		while ((l = inStream.read(buf)) >= 0) {
			outStream.write(buf, 0, l);
		}
		inStream.close();
		outStream.close();
	}

}
