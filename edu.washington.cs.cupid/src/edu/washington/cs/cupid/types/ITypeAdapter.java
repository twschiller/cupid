package edu.washington.cs.cupid.types;

import com.google.common.reflect.TypeToken;

/**
 * Interface for defining a type correspondence
 * 
 * @author Todd Schiller
 * @param <I> the input type
 * @param <V> the output value type
 */
public interface ITypeAdapter<I,V> {

	public TypeToken<I> getInputType();
	public TypeToken<V> getOutputType();
	
	public V adapt(I input);
}
