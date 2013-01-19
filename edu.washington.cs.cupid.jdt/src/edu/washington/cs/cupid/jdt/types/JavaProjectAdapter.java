package edu.washington.cs.cupid.jdt.types;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.types.ITypeAdapter;

public class JavaProjectAdapter implements ITypeAdapter<IProject, IJavaProject>{

	@Override
	public TypeToken<IProject> getInputType() {
		return TypeToken.of(IProject.class);
	}

	@Override
	public TypeToken<IJavaProject> getOutputType() {
		return TypeToken.of(IJavaProject.class);
	}

	@Override
	public IJavaProject adapt(IProject input) {
		return JavaCore.create(input);
	}

}
