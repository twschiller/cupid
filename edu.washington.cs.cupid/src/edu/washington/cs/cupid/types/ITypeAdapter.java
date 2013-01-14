package edu.washington.cs.cupid.types;

import com.google.common.reflect.TypeToken;

/**
 * A type adapter.
 * @author Todd Schiller
 * @param <I> the input type
 * @param <V> the output value type
 */
public interface ITypeAdapter<I, V> {

	// TODO improve interface documentation.
	
	/**
	 * Returns the input type for this adapter.
	 * @return the input type for this adapter.
	 */
	TypeToken<I> getInputType();
	
	/**
	 * Returns the output type for this adapter.
	 * @return the output type for this adapter.
	 */
	TypeToken<V> getOutputType();
	
	/**
	 * Adapt <code>input</code>. Returns <code>null</code> iff <code>input</code> is <code>null</code>.
	 * @param input the input
	 * @return the adapted value
	 */
	V adapt(I input);
}
