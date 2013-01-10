package edu.washington.cs.cupid.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ChangeNotifier;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.capability.ICapabilityRegistry;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;
import edu.washington.cs.cupid.preferences.PreferenceConstants;
import edu.washington.cs.cupid.views.ViewRule;

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
	
	private final IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
	
	
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
			if (TypeManager.isCompatible(capability, type)){
				result.add(capability);
			}
		}
		return result;	
	}
	
	@Override
	public synchronized Set<ICapability<?, ?>> getCapabilities(TypeToken<?> inputType, TypeToken<?> outputType) {
		Set<ICapability<?,?>> result = Sets.newIdentityHashSet();
		for (ICapability<?,?> capability : capabilities){
			
			if (TypeManager.isCompatible(capability, inputType) &&
				TypeManager.isJavaCompatible(outputType, capability.getReturnType())){
			
				result.add(capability);
			}
		}
		return result;	
	}
	
	@Override
	public synchronized Set<ICapability<?,?>> getCapabilitiesForOutput(TypeToken<?> outputType) {
		Set<ICapability<?,?>> result = Sets.newIdentityHashSet();
		for (ICapability<?,?> capability : capabilities){
			
			if (TypeManager.isJavaCompatible(outputType, capability.getReturnType())){
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
			if (TypeManager.isJavaCompatible(TypeToken.of(Boolean.class), capability.getReturnType())){
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

	@Override
	public ICapability<?, String> getViewer(TypeToken<?> type) {
		List<ViewRule> rules = new Gson().fromJson(
				preferences.getString(PreferenceConstants.P_TYPE_VIEWS),
				new com.google.gson.reflect.TypeToken<List<ViewRule>>(){}.getType());
		
		for (ViewRule rule : rules){
			if (rule.isActive() && rule.getCapability() != null){
				ICapability<?, ?> capability;
				try {
					capability = findCapability(rule.getCapability());
				} catch (NoSuchCapabilityException e) {
					continue;
				}
				
				if (TypeManager.isCompatible(capability, type)){
					return (ICapability<?, String>) capability;
				}
			}
		}
		return null;
	}
}
