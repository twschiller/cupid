package edu.washington.cs.cupid.capability;

/**
 * <p>Notifies listeners to changes in the set of available capabilities from an {@link ICapabilityPublisher}.</p>
 * 
 * <p>Since notification is synchronous with the activity itself, the listener should provide a fast and robust implementation. 
 * If the handling of notifications would involve blocking operations, or operations which might throw uncaught exceptions, 
 * the notifications should be queued, and the actual processing deferred (or perhaps delegated to a separate thread).</p>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @see {@link ChangeNotifier} a thread-safe change notifier
 */
public interface ICapabilityChangeNotifier {
	/**
	 * Add a listener to be notified when the set of available capabilities
	 * provided by this publisher changes. Does nothing if <code>listener</code>
	 * is already in the notification set.
	 * @param listener the non-<code>null</code> listener to add 
	 */
	void addChangeListener(ICapabilityChangeListener listener);
	
	/**
	 * Removes a listener from the notification set. Does nothing if <code>listener</code>
	 * is not in the notification set.
	 * @param listener the non-<code>null</code> listener to remove
	 */
	void removeChangeListener(ICapabilityChangeListener listener);
}
