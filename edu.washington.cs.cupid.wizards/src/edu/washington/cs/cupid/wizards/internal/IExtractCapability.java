package edu.washington.cs.cupid.wizards.internal;

import java.io.Serializable;

import edu.washington.cs.cupid.capability.ICapability;

/**
 * Interface for capabilities that extract information from other results
 * @author Todd Schiller
 */
public interface IExtractCapability<I,V> extends ICapability<I,V>, Serializable {
	
}
