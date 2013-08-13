package edu.washington.cs.cupid.jdt.types;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.types.ITypeAdapter;

public class CompilationUnitAdapter implements ITypeAdapter<ICompilationUnit, IFile> {

	@Override
	public TypeToken<ICompilationUnit> getInputType() {
		return TypeToken.of(ICompilationUnit.class);
	}

	@Override
	public TypeToken<IFile> getOutputType() {
		return TypeToken.of(IFile.class);
	}

	@Override
	public IFile adapt(ICompilationUnit input) {
		try {
			return (IFile) input.getCorrespondingResource();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
}
