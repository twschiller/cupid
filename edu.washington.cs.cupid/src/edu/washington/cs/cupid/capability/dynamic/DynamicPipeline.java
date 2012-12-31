package edu.washington.cs.cupid.capability.dynamic;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;

/**
 * A pipeline
 * @author Todd Schiller
 */
public abstract class DynamicPipeline<I,V> implements ICapability<I,V>, Serializable{

	private static final long serialVersionUID = 1L;

	private final String name;
	private final String description;
	private final Set<Object> capabilities;
	
	public DynamicPipeline(String name, String description, Collection<Object> capabilities){
		this.name = name;
		this.description = description;
		this.capabilities = Sets.newHashSet(capabilities);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Map<String, ICapability<?,?>> current() throws NoSuchCapabilityException{
		Map<String, ICapability<?,?>> result = Maps.newHashMap();
		for (Object capability : capabilities){
			if (capability instanceof ICapability){
				ICapability<?,?> x = (ICapability<?,?>) capability;
				result.put(x.getUniqueId(), x);
			}else if (capability instanceof String){
				result.put((String) capability, CupidPlatform.getCapabilityRegistry().findCapability((String) capability));
			}else{
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
		}
		return result;
	}
	

	@Override
	public boolean isPure() {
		try {
			return Iterables.all(current().values(), new Predicate<ICapability<?,?>>(){
				@Override
				public boolean apply(ICapability<?, ?> capability) {
					return capability.isPure();
				}
			});
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}
	
	@Override
	public boolean isLocal() {
		try {
			return Iterables.all(current().values(), new Predicate<ICapability<?,?>>(){
				@Override
				public boolean apply(ICapability<?, ?> capability) {
					return capability.isLocal();
				}
			});
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}
	
	@Override
	public boolean isTransient() {
		try {
			return Iterables.any(current().values(), new Predicate<ICapability<?,?>>(){
				@Override
				public boolean apply(ICapability<?, ?> capability) {
					return capability.isPure();
				}
			});
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}
	

	@Override
	public Set<String> getDynamicDependencies() {
		Set<String> dependencies = Sets.newHashSet();
		
		for (Object capability : capabilities){
			if (capability instanceof String){
				dependencies.add((String)capability);
			}
		}
	
		return dependencies;	
	}

}
