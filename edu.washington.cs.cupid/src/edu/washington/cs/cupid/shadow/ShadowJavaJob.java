package edu.washington.cs.cupid.shadow;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.synchronization.ProjectSynchronizer;

/**
 * A {@link Job} that produces a reference to the {@link IJavaElement} in the corresponding
 * shadow project.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class ShadowJavaJob extends Job implements IShadowJob<IJavaElement> {

	private final IJavaElement element;
	private IJavaElement shadow = null;
	
	/**
	 * @param element the element in the real project
	 */
	public ShadowJavaJob(IJavaElement element) {
		super("Shadow element " + element.getElementName());
		this.element = element;
	}
	
	@Override
	public IJavaElement get() {
		return shadow;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IProject project = element.getJavaProject().getProject();
		ProjectSynchronizer sProject = CupidActivator.getShadowManager().getSynchronizer(project);
		
		if (element instanceof IJavaProject){
			shadow = JavaCore.create(sProject.getShadowProject());
			
			try {
				sProject.getShadowProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			} catch (CoreException ex) {
				throw new RuntimeException(new InvocationTargetException(ex));
			}
		}else{
			IJavaProject jShadow = JavaCore.create(sProject.getShadowProject());
			IPath relative = element.getPath().makeRelativeTo(element.getJavaProject().getPath().append("src"));
		
			try {
				shadow = jShadow.findElement(relative);
			} catch (JavaModelException ex) {
				throw new RuntimeException(new InvocationTargetException(ex));
			}
			
			if (shadow == null){
				throw new RuntimeException("Could not find Java element " + relative.toString());
			}
			
			try {
				sProject.getShadowProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			} catch (CoreException ex) {
				throw new RuntimeException(new InvocationTargetException(ex));
			}	
		}
		
		return Status.OK_STATUS;
	}
}
