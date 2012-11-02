package edu.washington.cs.cupid.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.capability.ChangeNotifier;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.capability.ICapabilityRegistry;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;

/**
 * A thread-safe registry of the available Cupid capabilities
 * @author Todd Schiller (tws@cs.washington.edu) 
 */
public class CapabilityRegistry implements ICapabilityRegistry{

	/**
	 * Set of available capabilities, organized by contributing publisher
	 */
	private final HashMap<ICapabilityPublisher, Set<ICapability<?,?>>> capabilityMap = Maps.newHashMap();

	/**
	 * Set of available capabilities
	 */
	private final Set<ICapability<?,?>> capabilities = Sets.newIdentityHashSet();
	
	/**
	 * Set of change listeners
	 */
	private final ChangeNotifier notifier = new ChangeNotifier();
	
	@Override
	public synchronized void onChange(ICapabilityPublisher publisher) {
		Set<ICapability<?,?>> available = Sets.newHashSet(publisher.publish());
		
		if (capabilityMap.containsKey(publisher)){
			Set<ICapability<?,?>> old = capabilityMap.get(publisher);	
			Set<ICapability<?,?>> removed = Sets.difference(old, available);
			capabilities.removeAll(removed);
		}
		
		capabilityMap.put(publisher, available);
		capabilities.addAll(available);
		
		notifier.onChange(this);
	}
	
	@Override
	public synchronized ICapability<?, ?>[] publish() {
		return capabilities.toArray(new ICapability<?,?>[]{});
	}

	@Override
	public synchronized ICapability<?,?> findCapability(final String uniqueId) throws NoSuchCapabilityException{
		for (ICapability <?,?> capability : capabilities){
			if (capability.getUniqueId().equals(uniqueId)){
				return capability;
			}	
		}
		throw new NoSuchCapabilityException(uniqueId);
	}

	@Override
	public synchronized Set<ICapability<?,?>> getCapabilities(TypeToken<?> type){
		Set<ICapability<?,?>> result = Sets.newIdentityHashSet();
		for (ICapability<?,?> capability : capabilities){
			if (CapabilityExecutor.isCompatible(capability, type)){
				result.add(capability);
			}
		}
		return result;	
	}
	
	@Override
	public synchronized Set<ICapability<?, ?>> getCapabilities(TypeToken<?> inputType, TypeToken<?> outputType) {
		Set<ICapability<?,?>> result = Sets.newIdentityHashSet();
		for (ICapability<?,?> capability : capabilities){
			
			if (CapabilityExecutor.isCompatible(capability, inputType) &&
				CapabilityExecutor.isResultCompatible(capability, outputType)){
			
				result.add(capability);
			}
		}
		return result;	
	}
	
	@Override
	public synchronized Set<ICapability<?,?>> getCapabilitiesForOutput(TypeToken<?> outputType) {
		Set<ICapability<?,?>> result = Sets.newIdentityHashSet();
		for (ICapability<?,?> capability : capabilities){
			
			if (CapabilityExecutor.isResultCompatible(capability, outputType)){
				result.add(capability);
			}
		}
		return result;	
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized Set<ICapability<?, Boolean>> getPredicates() {
		Set<ICapability<?,Boolean>> result = Sets.newIdentityHashSet();
		for (ICapability<?,?> capability : capabilities){
			
			if (CapabilityExecutor.isResultCompatible(capability, TypeToken.of(Boolean.class))){
				result.add((ICapability<?, Boolean>) capability);
			}
		}
		return result;	
	}

	@Override
	public synchronized Set<ICapability<?,?>> getCapabilities(){
		return Collections.unmodifiableSet(capabilities);
	}
	
	@Override
	public synchronized void addChangeListener(ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}

	@Override
	public synchronized void removeChangeListener(ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}
	
	@Override
	public synchronized void registerPublisher(ICapabilityPublisher publisher){
		publisher.addChangeListener(this);
		onChange(publisher);
	}

	@Override
	public void registerStaticCapability(ICapability<?, ?> capability) {
		capabilities.add(capability);
		notifier.onChange(this);
	}
}
