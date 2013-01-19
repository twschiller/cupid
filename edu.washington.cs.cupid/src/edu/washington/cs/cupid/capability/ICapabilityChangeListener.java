package edu.washington.cs.cupid.capability;

/**
 * <p>Listen for changes to the set of capabilities provided by an {@link ICapabilityPublisher}.</p>
 * 
 * <p>Since notification is synchronous with the activity itself, the listener should provide a fast and robust implementation. 
 * If the handling of notifications would involve blocking operations, or operations which might throw uncaught exceptions, 
 * the notifications should be queued, and the actual processing deferred (or perhaps delegated to a separate thread).</p>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @see {@link ICapabilityPublisher} the capability publishing interface
 * @see {@link ChangeNotifier} a thread-safe notification implementation
 */
public interface ICapabilityChangeListener {
	
	/**
	 * Triggered when the set of capabilities provided by <code>publisher</code> changes.
	 * @param publisher the notifying publisher
	 */
	void onChange(final ICapabilityPublisher publisher);
}
