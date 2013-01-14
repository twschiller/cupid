package edu.washington.cs.cupid.types;

/**
 * Interface for objects that define Cupid type adapters.
 * @see ITypeAdapter
 * @author Todd Schiller
 */
public interface ITypeAdapterPublisher {

	/**
	 * Returns the published type adapters.
	 * @return the published type adapters.
	 */
	ITypeAdapter<?, ?>[] publish();
	
}
