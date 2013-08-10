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

import org.eclipse.core.resources.WorkspaceJob;
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
import org.osgi.framework.Bundle;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.washington.cs.cupid.scripting.java.CupidScriptingPlugin;

/**
 * Job that attempts to automatically update the libraries (JAR files) on the Cupid scripting path, since
 * updating a plug-in changes the filename. Uses the scheduling rule of the Cupid Java project.
 * @author Todd Schiller
 */
public final class UpdateClasspathJob extends WorkspaceJob implements ISchedulingRule {

	/**
	 * Create a job that updates the Cupid classpath.
	 */
	public UpdateClasspathJob() {
		super("Update Cupid Classpath");
		super.setRule(CupidScriptingPlugin.getDefault().getCupidJavaProject().getSchedulingRule());
	}
	
	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) {
		try {
			IJavaProject project = CupidScriptingPlugin.getDefault().getCupidJavaProject();
			
			int classpathSize = project.getRawClasspath().length;
			
			monitor.beginTask("Update Cupid Classpath", classpathSize + 2);
			
			List<IClasspathEntry> updatedClasspath = Lists.newArrayList();
			List<IClasspathEntry> updatedEntries = Lists.newArrayList();
			
			List<String> allResolved = Lists.newArrayList();
			
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
							// we found a bundle with the same name
							IPath replacement = ClasspathUtil.bundlePath(bundle);
							IClasspathEntry newEntry = JavaCore.newLibraryEntry(replacement, null, null);
							updatedClasspath.add(newEntry);
							updatedEntries.add(newEntry);
						}else{
							// we can't find a bundle with the same name (e.g., b/c it was uninstalled)
							CupidScriptingPlugin.getDefault().logWarning(
									"Can't find updated bundle for Cupid Project classpath entry: " + entry.getPath());
						
							updatedClasspath.add(entry);
						}
					} else {
						CupidScriptingPlugin.getDefault().logWarning(
								"Can't parse Cupid Project classpath entry: " + entry.getPath());
						
						// we don't know how to find the name
						updatedClasspath.add(entry);
					}
				} else if (resolved == null){
					CupidScriptingPlugin.getDefault().logWarning(
							"Can't resolve non-library Cupid Project classpath entry: " + entry.getPath());
					
					// we don't know how to 
					updatedClasspath.add(entry);
				} else {
					// don't change entries we can resolve
					updatedClasspath.add(entry);
					allResolved.add(entry.getPath().toString());
				}	
				monitor.worked(1);
			}
			
			CupidScriptingPlugin.getDefault().logInformation(
					"Resolved " + allResolved.size() + " Cupid classpath entries (see log for details)" + System.getProperty("line.separator") + Joiner.on(System.getProperty("line.separator")).join(allResolved));
			
			if (!updatedEntries.isEmpty()) {
				// perform update
				project.setRawClasspath(updatedClasspath.toArray(new IClasspathEntry[]{}), new SubProgressMonitor(monitor, 1));
			
				for (IClasspathEntry entry : updatedEntries){
					CupidScriptingPlugin.getDefault().logInformation(
							"Updated Cupid classpath entry " + entry.getPath());
				}
			}else{
				CupidScriptingPlugin.getDefault().logInformation(
						"Cupid Project classpath is up-to-date");
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
