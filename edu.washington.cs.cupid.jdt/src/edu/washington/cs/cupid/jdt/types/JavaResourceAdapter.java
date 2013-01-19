package edu.washington.cs.cupid.jdt.types;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.types.ITypeAdapter;

public class JavaResourceAdapter implements ITypeAdapter<IJavaElement, IResource> {

	@Override
	public TypeToken<IJavaElement> getInputType() {
		return TypeToken.of(IJavaElement.class);
	}

	@Override
	public TypeToken<IResource> getOutputType() {
		return TypeToken.of(IResource.class);
	}

	@Override
	public IResource adapt(IJavaElement input){
		try {
			return input.getCorrespondingResource();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
}
