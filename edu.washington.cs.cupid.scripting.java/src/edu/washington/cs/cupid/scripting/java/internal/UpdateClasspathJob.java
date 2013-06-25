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

import edu.washington.cs.cupid.scripting.java.CupidScriptingPlugin;

/**
 * Job that attempts to automatically update the libraries (JAR files) on the Cupid scripting path, since
 * updating a plug-in changes the filename. Uses the scheduling rule of the Cupid Java project.
 * @author Todd Schiller
 */
public final class UpdateClasspathJob extends UIJob implements ISchedulingRule {

	/**
	 * Create a job that updates the Cupid classpath.
	 */
	public UpdateClasspathJob() {
		super("Update Cupid Classpath");
		super.setRule(CupidScriptingPlugin.getDefault().getCupidJavaProject().getSchedulingRule());
	}
	
	@Override
	public IStatus runInUIThread(final IProgressMonitor monitor) {
		try {
			IJavaProject project = CupidScriptingPlugin.getDefault().getCupidJavaProject();
			
			int classpathSize = project.getRawClasspath().length;
			boolean any = false;
			
			monitor.beginTask("Update Cupid Classpath", classpathSize + 2);
			
			List<IClasspathEntry> updated = Lists.newArrayList();
			
			for (IClasspathEntry entry : project.getRawClasspath()) {
				IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(entry);
				
				if (resolved == null && entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY){
					// try to find bundle with same name
					IPath path = entry.getPath();
					String filename = path.segment(path.segmentCount() - 1);
					
					String [] parts = filename.split("_");
					if (parts.length == 2) {
						Bundle bundle = Platform.getBundle(parts[0]);
						if (bundle != null) {
							IPath replacement = ClasspathUtil.bundlePath(bundle);
							updated.add(JavaCore.newLibraryEntry(replacement, null, null));
							any = true;
						}
					} else {
						// we don't know how to find the name
						updated.add(entry);
					}
				} else {
					// don't change entries we can resolve
					updated.add(entry);
				}	
				monitor.worked(1);
			}
			
			if (any) {
				// perform update
				project.setRawClasspath(updated.toArray(new IClasspathEntry[]{}), new SubProgressMonitor(monitor, 1));
			}
			
			return Status.OK_STATUS;
		} catch (Exception ex) {
			return new Status(IStatus.ERROR, CupidScriptingPlugin.PLUGIN_ID, "Error updating Cupid scripting classpath", ex);
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
		return CupidScriptingPlugin.getDefault().getCupidJavaProject().getSchedulingRule().isConflicting(rule);
	}
}
