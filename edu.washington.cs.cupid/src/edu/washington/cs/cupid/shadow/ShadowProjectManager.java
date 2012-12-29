package edu.washington.cs.cupid.shadow;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Maps;

import edu.washington.cs.synchronization.ProjectSynchronizer;

/**
 * Manages Cupid shadow projects
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class ShadowProjectManager {
	
	public static String SYNCHRONIZER_PREFIX = "CUPID";

	/**
	 * Project synchronizers
	 */
	private final HashMap<IProject, ProjectSynchronizer> synchronizers = Maps.newHashMap();
	
	public ShadowProjectManager(){
	}
	
	public ProjectSynchronizer getSynchronizer(IProject project){
		return synchronizers.get(project);
	}
	
	public void stopAll(){
		for (ProjectSynchronizer synchronizer : synchronizers.values()){
			synchronizer.stop();
		}
	}
	
	public static IProject createShadowProject(IProject project){
		ProjectSynchronizer sync = new ProjectSynchronizer(SYNCHRONIZER_PREFIX, project);
		
		sync.init(); sync.stop();
		
		IProject result = sync.getShadowProject();
		
		if (result == null){
			throw new RuntimeException("Error creating shadow project for " + project.getName());
		}
		
		try {
			result.build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (CoreException e) {
			throw new RuntimeException("Error building shadow project for " + project.getName());
		}
		
		return result;
	}
}
