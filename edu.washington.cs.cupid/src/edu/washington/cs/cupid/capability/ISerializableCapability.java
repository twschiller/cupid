package edu.washington.cs.cupid.capability;

import java.io.Serializable;

/**
 * A capability that can be serialized (e.g., saved to disk and restored).
 * @author Todd Schiller
 * @param <I> input type
 * @param <V> output type
 */
public interface ISerializableCapability<I, V> extends ICapability<I, V>, Serializable {

}
