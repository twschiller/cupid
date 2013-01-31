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

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

/**
 * Job that attempts to automatically update the libraries (JAR files) on the Cupid scripting path, since
 * updating a plug-in changes the filename. Uses the scheduling rule of the Cupid Java project.
 * @author Todd Schiller
 */
public final class UpdateCupidClasspath extends UIJob implements ISchedulingRule {

	/**
	 * Create a job that updates the Cupid classpath.
	 */
	public UpdateCupidClasspath() {
		super("Update Cupid Classpath");
	}
	
	@Override
	public IStatus runInUIThread(final IProgressMonitor monitor) {
		try {
			IJavaProject project = Activator.getDefault().getCupidJavaProject();
			
			int work = project.getRawClasspath().length;
			boolean any = false;
			
			monitor.beginTask("Update Cupid Classpath", work + 2);
			
			List<IClasspathEntry> updated = Lists.newArrayList(project.getRawClasspath());
			
			for (IClasspathEntry entry : project.getRawClasspath()) {
				IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(entry);
				
				IPath replacement = null;
				
				if (resolved == null && entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					
					// try to find bundle with same name
					IPath path = entry.getPath();
					String filename = path.segment(path.segmentCount() - 1);
					
					String [] parts = filename.split("_");
					if (parts.length == 2) {
						Bundle bundle = Platform.getBundle(parts[0]);
						if (bundle != null) {
							replacement = ClasspathUtil.bundlePath(bundle);
							any = true;
						}
					}
				}
				
				updated.add(replacement == null ? entry : JavaCore.newLibraryEntry(replacement, null, null));
			
				monitor.worked(1);
			}
			
			if (any) {
				// clear old path
				project.setRawClasspath(null, new SubProgressMonitor(monitor, 1));
				
				// perform update
				project.setRawClasspath(updated.toArray(new IClasspathEntry[]{}), new SubProgressMonitor(monitor, 1));
			}
			
			return Status.OK_STATUS;
		} catch (Exception ex) {
			return new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Error updating Cupid scripting classpath", ex);
		} finally {
			monitor.done();
		}
	}

	@Override
	public boolean contains(final ISchedulingRule rule) {
		return false;
	}

	@Override
	public boolean isConflicting(final ISchedulingRule rule) {
		return Activator.getDefault().getCupidJavaProject().getSchedulingRule().isConflicting(rule);
	}
}
