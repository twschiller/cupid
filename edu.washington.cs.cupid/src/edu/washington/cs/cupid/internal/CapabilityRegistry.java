/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.internal;

import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ChangeNotifier;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;
import edu.washington.cs.cupid.capability.ICapabilityRegistry;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.utility.CapabilityUtil;

/**
 * A thread-safe registry of the available Cupid capabilities.
 * @author Todd Schiller (tws@cs.washington.edu) 
 */
public final class CapabilityRegistry implements ICapabilityRegistry {

	/**
	 * Set of available capabilities, organized by contributing publisher.
	 */
	private final HashMap<ICapabilityPublisher, Set<ICapability>> capabilityMap = Maps.newHashMap();

	/**
	 * Set of available capabilities.
	 */
	private final Set<ICapability> capabilities = Sets.newIdentityHashSet();
	
	/**
	 * Set of change listeners.
	 */
	private final ChangeNotifier notifier = new ChangeNotifier();
	
	@Override
	public synchronized void onChange(final ICapabilityPublisher publisher) {
		Set<ICapability> available = Sets.newHashSet(publisher.publish());
		
		if (capabilityMap.containsKey(publisher)) {
			Set<ICapability> old = capabilityMap.get(publisher);	
			Set<ICapability> removed = Sets.difference(old, available);
			capabilities.removeAll(removed);
		}
		
		capabilityMap.put(publisher, available);
		capabilities.addAll(available);
		
		notifier.onChange(this);
	}
	
	@Override
	public synchronized ICapability[] publish() {
		return capabilities.toArray(new ICapability[]{});
	}

	@Override
	public synchronized ICapability findCapability(final String uniqueId) throws NoSuchCapabilityException {
		for (ICapability capability : capabilities) {
			if (capability.getUniqueId().equals(uniqueId)) {
				return capability;
			}	
		}
		throw new NoSuchCapabilityException(uniqueId);
	}

	@Override
	public synchronized SortedSet<ICapability> getCapabilities(final TypeToken<?> type) {
		SortedSet<ICapability> result = Sets.newTreeSet(CapabilityUtil.COMPARE_NAME);
		for (ICapability capability : capabilities) {
			for (ICapability.Parameter<?> param :  capability.getParameters()){
				if (TypeManager.isCompatible(param, type)) {
					result.add(capability);
					break;
				}
			}
		}
		return result;	
	}
	
	@Override
	public synchronized SortedSet<ICapability> getCapabilities(final TypeToken<?> inputType, final TypeToken<?> outputType) {
		SortedSet<ICapability> result = Sets.newTreeSet(CapabilityUtil.COMPARE_NAME);
		
		for (ICapability capability : getCapabilities(inputType)) {
			for (ICapability.Output<?> output : capability.getOutputs()){
				if (TypeManager.isJavaCompatible(outputType, output.getType())) {
					result.add(capability);
					break;
				}
			}
		}
		return result;	
	}
	
	@Override
	public synchronized SortedSet<ICapability> getCapabilitiesForOutput(final TypeToken<?> outputType) {
		SortedSet<ICapability> result = Sets.newTreeSet(CapabilityUtil.COMPARE_NAME);
		
		for (ICapability capability : capabilities) {
			for (ICapability.Output<?> output : capability.getOutputs()){
				if (TypeManager.isJavaCompatible(outputType, output.getType())) {
					result.add(capability);
					break;
				}
			}
		}
		return result;	
	}
	

	@Override
	public SortedSet<ICapability> getCapabilities(Predicate<ICapability> filter) {
		SortedSet<ICapability> result = Sets.newTreeSet(CapabilityUtil.COMPARE_NAME);
		for (ICapability capability : capabilities) {
			if (filter.apply(capability)){
				result.add(capability);
			}
		}
		return result;
	}
	
	
	@Override
	public synchronized SortedSet<ICapability> getPredicates() {
		SortedSet<ICapability> result = Sets.newTreeSet(CapabilityUtil.COMPARE_NAME);
		
		for (ICapability capability : capabilities) {
			Set<ICapability.Output<?>> bool = Sets.newHashSet();
			for (ICapability.Output<?> output : capability.getOutputs()){
				if (TypeManager.isJavaCompatible(TypeToken.of(Boolean.class), output.getType())) {
					bool.add(output);
				}
			}
			if (bool.size() == 1){
				result.add(capability);
			}
		}
		return result;	
	}

	@Override
	public synchronized SortedSet<ICapability> getCapabilities() {
		SortedSet<ICapability> result = Sets.newTreeSet(CapabilityUtil.COMPARE_NAME);
		result.addAll(capabilities);
		return result;
	}
	
	@Override
	public synchronized void addChangeListener(final ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}

	@Override
	public synchronized void removeChangeListener(final ICapabilityChangeListener listener) {
		notifier.addChangeListener(listener);
	}
	
	@Override
	public synchronized void registerPublisher(final ICapabilityPublisher publisher) {
		publisher.addChangeListener(this);
		onChange(publisher);
	}
	
	@Override
	public void registerStaticCapability(final ICapability capability) {
		capabilities.add(capability);
		notifier.onChange(this);
	}

	@Override
	public ICapability getViewer(final TypeToken<?> type) {
		throw new UnsupportedOperationException();
		
//		List<ViewRule> rules = new Gson().fromJson(
//				preferences.getString(PreferenceConstants.P_TYPE_VIEWS),
//				new com.google.gson.reflect.TypeToken<List<ViewRule>>() { } .getType());

//		for (ViewRule rule : rules) {
//			if (rule.isActive() && rule.getCapability() != null) {
//				ICapability capability;
//				try {
//					capability = findCapability(rule.getCapability());
//				} catch (NoSuchCapabilityException e) {
//					continue;
//				}
//				
//				if (TypeManager.isCompatible(capability, type)) {
//					return capability;
//				}
//			}
//		}
//		return null;
	}

}
