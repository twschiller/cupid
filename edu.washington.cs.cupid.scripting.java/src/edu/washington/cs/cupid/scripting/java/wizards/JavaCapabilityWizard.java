package edu.washington.cs.cupid.scripting.java.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.google.common.collect.Sets;

import edu.washington.cs.cupid.scripting.java.internal.Activator;

public class JavaCapabilityWizard extends Wizard implements INewWizard {
	private JavaCapabilityWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for JavaCapabilityWizard.
	 */
	public JavaCapabilityWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new JavaCapabilityWizardPage(selection);
		addPage(page);
	}
	
	private String formClassName(String name){
		return name.replaceAll(" ", "");
	}
	
	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
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
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(name, id, description, parameterType, returnType,  monitor);
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
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @throws IOException 
	 */

	private void doFinish(String name, String id, String description, Class<?> parameterType, Class<?> returnType, IProgressMonitor monitor) throws Exception{
				
		// create a sample file
		String className = formClassName(name);
		
		monitor.beginTask("Creating " + name, 2);
		
		IProject cupid = Activator.getDefault().getCupidProject();
		
		final IFile file = cupid.getFolder("src").getFile(new Path(className + ".java"));
		
		file.create(openContents(name, id, description, parameterType, returnType, cupid.getDefaultCharset()), true, monitor);
	
		monitor.worked(1);
		
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(2);
	}
	
	private InputStream openContents(String name, String id, String description, Class<?> paramType, Class<?> returnType, String charSet) throws UnsupportedEncodingException, ParserConfigurationException, TransformerException{
		String className = formClassName(name);
		String separator = System.getProperty("line.separator");
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("import edu.washington.cs.cupid.capability.AbstractCapability;").append(separator);
		builder.append("import edu.washington.cs.cupid.capability.CapabilityJob;").append(separator);
		builder.append("import edu.washington.cs.cupid.jobs.ImmediateJob;").append(separator);
		
		for (String clazz : Sets.newHashSet(paramType.getName(), returnType.getName())){
			builder.append("import " + clazz + ";").append(separator);
		}
		
		builder.append("public class " + className + " extends AbstractCapability<" + paramType.getSimpleName() + "," + returnType.getSimpleName() + ">{").append(separator);
		
		String [] ctorLines = new String[]{
				"public " + className + "(){",
				"\tsuper(\"" + className + "\", \"" + id + "\",",
				"\t\"" + description + "\",",
				"\t" + paramType.getSimpleName() + ".class, " + returnType.getSimpleName() + ".class,",
				"\tFlag.PURE, Flag.LOCAL);",
				"}"
		};
		
		for (String line : ctorLines){
			builder.append("\t").append(line).append(separator);
		}
	
		String [] lines = new String[]{
				"@Override",
				"public CapabilityJob<" + paramType.getSimpleName() + ", " + returnType.getSimpleName() + "> getJob(" + paramType.getSimpleName() + " input) {",
				"\treturn null;",
				"}"
		};
		
		for (String line : lines){
			builder.append("\t").append(line).append(separator);
		}

		builder.append("}");
		
		return new ByteArrayInputStream(builder.toString().getBytes(charSet));
	}
	
	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}