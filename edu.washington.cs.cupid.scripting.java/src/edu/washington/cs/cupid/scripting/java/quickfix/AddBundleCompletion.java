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
package edu.washington.cs.cupid.scripting.java.quickfix;

import java.util.List;

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

import edu.washington.cs.cupid.scripting.java.internal.ClasspathUtil;

/**
 * Quick Fix completion that adds a bundle to a Java project classpath.
 * @author Todd Schiller
 */
public final class AddBundleCompletion implements IJavaCompletionProposal {

	private static final int RELEVANCE = 100;
	private IJavaProject project;
	private Bundle bundle;
	
	/**
	 * Construct a Quick Fix completion that adds a bundle to the Java project classpath.
	 * @param project the Java project
	 * @param bundle the bundle to add to the classpath
	 */
	public AddBundleCompletion(final IJavaProject project, final Bundle bundle) {
		this.project = project;
		this.bundle = bundle;
	}
	
	@Override
	public int getRelevance() {
		return RELEVANCE;
	}

	@Override
	public void apply(final IDocument document) {
		try {
			List<IClasspathEntry> cp = Lists.newArrayList(project.getRawClasspath());
			
			IPath path = ClasspathUtil.bundlePath(bundle);
			if (path != null) {
				cp.add(JavaCore.newLibraryEntry(path, null, null));	
			}
			
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
		return "Add " + this.bundle.getSymbolicName() + " to the Cupid project classpath";
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

}
