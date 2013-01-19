package edu.washington.cs.cupid.jdt.types;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.types.ITypeAdapter;

public class ProjectAdapter implements ITypeAdapter<IJavaProject, IProject>{

	@Override
	public TypeToken<IJavaProject> getInputType() {
		return TypeToken.of(IJavaProject.class);
	}

	@Override
	public TypeToken<IProject> getOutputType() {
		return TypeToken.of(IProject.class);
	}

	@Override
	public IProject adapt(IJavaProject input) {
		return input.getProject();
	}

}
