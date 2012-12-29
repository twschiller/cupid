package edu.washington.cs.cupid.capability;

/**
 * Interface for classes that provide a set of Cupid capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface ICapabilityPublisher extends ICapabilityChangeNotifier {
	
	/**
	 * Returns the current set available capabilities. Capabilities are referentially stable.
	 * @return the available capabilities
	 */
	ICapability<?,?> [] publish();
}
