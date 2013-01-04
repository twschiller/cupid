package edu.washington.cs.cupid.types;

import com.google.common.reflect.TypeToken;

/**
 * Registry of type correspondences
 * @author Todd Schiller
 */
public interface ITypeAdapterRegistry {
	public void registerAdapter(ITypeAdapter<?,?> adapter);
	public ITypeAdapter<?,?>[] getTypeAdapters(TypeToken<?> inputType);
	public ITypeAdapter<?,?> getTypeAdapter(TypeToken<?> inputType, TypeToken<?> outputType);
}
