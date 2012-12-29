package edu.washington.cs.cupid.capability;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * <p>Capability change notifier that automatically rebroadcasts the changes from
 * any {@link ICapabilityPublisher}s it is listening to.</p>
 * 
 * <p>Does <i>not</i> check for notification loops.</p>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class ChangeNotifier implements ICapabilityChangeListener, ICapabilityChangeNotifier{ 

	private final Set<ICapabilityChangeListener> listeners = Sets.newIdentityHashSet();
		
	public ChangeNotifier() {
		super();
	}
	
	@Override
	public void onChange(final ICapabilityPublisher publisher) {
		for (final ICapabilityChangeListener listener : listeners){
			listener.onChange(publisher);
		}
	}

	@Override
	public synchronized void addChangeListener(ICapabilityChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public synchronized void removeChangeListener(ICapabilityChangeListener listener) {
		listeners.remove(listener);
	}
}
