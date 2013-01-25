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
package edu.washington.cs.cupid.scripting.java.wizards;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import com.floreysoft.jmte.Engine;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import edu.washington.cs.cupid.scripting.java.internal.Activator;

/**
 * A wizard for creating a new Java script capability.
 * @author Todd Schiller
 */
public final class JavaCapabilityWizard extends Wizard implements INewWizard {
	private JavaCapabilityWizardPage page;
	private ISelection selection;

	/**
	 * Construct a wizard for creating a new Java script capability.
	 */
	public JavaCapabilityWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		page = new JavaCapabilityWizardPage(selection);
		addPage(page);
	}
	
	private String formClassName(final String name) {
		return name.replaceAll(" ", "");
	}
	
	@Override
	public boolean performFinish() {
		final String name = page.getCapabilityName();
		final String description = page.getCapabilityDescription();
		final String id = page.getUniqueId();
		
		final Class<?> parameterType;
		final Class<?> returnType;
		
		try {
			parameterType = page.getParameterType();
			returnType = page.getReturnType();
		} catch (ClassNotFoundException ex) {
			MessageDialog.openError(getShell(), "Error", "Invalid parameter or return type");
			return false;
		}
		
		final List<IPath> classpath = Lists.newArrayList(page.getParameterTypeReference(), page.getOutputTypeReference());
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(name, id, description, parameterType, returnType, classpath,  monitor);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	private boolean inClasspath(List<IClasspathEntry> classpath, IPath query){
		for (IClasspathEntry entry : classpath){
			if (entry.getPath().equals(query)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @throws IOException 
	 */
	private void doFinish(final String name, final String id, final String description, final Class<?> parameterType, final Class<?> returnType, final List<IPath> classpath, final IProgressMonitor monitor) throws Exception {
				
		// create a sample file
		String className = formClassName(name);
		
		monitor.beginTask("Creating " + name, 2);
		
		IProject cupid = Activator.getDefault().getCupidProject();
		
		final IFile file = cupid.getFolder("src").getFile(new Path(className + ".java"));
		
		file.create(openContents(name, id, description, parameterType, returnType, cupid.getDefaultCharset()), true, monitor);
	
		IJavaProject proj = JavaCore.create(cupid);
		List<IClasspathEntry> cp = Lists.newArrayList(proj.getRawClasspath());
		for (IPath path : classpath) {
			if (path != null && !inClasspath(cp, path)) {
				cp.add(JavaCore.newLibraryEntry(path, null, null));	
			}
		}
		proj.setRawClasspath(cp.toArray(new IClasspathEntry[]{}), null);
		
		monitor.worked(1);
		
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage active = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(active, file);
				} catch (PartInitException e) {
					throw new RuntimeException("Error opening script editor", e);
				}
			}
		});
		
		monitor.done();
	}
	
	private InputStream openContents(final String name, final String id, final String description, final Class<?> paramType, final Class<?> returnType, final String charSet) throws Exception{
		String separator = System.getProperty("line.separator");
		
		Bundle bundle = Activator.getDefault().getBundle();
		URL fileURL = bundle.getEntry("templates/AbstractCapability.template");
		File file = new File(FileLocator.resolve(fileURL).toURI());
		
		String template = Joiner.on(separator).join(Files.readLines(file, Charset.defaultCharset()));
	
		Engine engine = new Engine();
		Map<String, Object> model = new HashMap<String, Object>();
		
		model.put("CLASS", formClassName(name));
		model.put("NAME", name);
		model.put("UNIQUE_ID", id);
		model.put("DESCRIPTION", description);
		model.put("INPUT_TYPE", paramType.getSimpleName());
		model.put("OUTPUT_TYPE", returnType.getSimpleName());
		model.put("IMPORTS", Lists.newArrayList(paramType.getName(), returnType.getName()));
		
		String content = engine.transform(template, model);
		
		return new ByteArrayInputStream(content.getBytes(charSet));
	}
	
	@Override 
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		this.selection = selection;
	}
}
