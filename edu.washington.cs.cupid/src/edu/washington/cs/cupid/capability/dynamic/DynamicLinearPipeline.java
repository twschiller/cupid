package edu.washington.cs.cupid.capability.dynamic;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;

/**
 * A linear pipeline with dynamic binding
 * @author Todd Schiller
 */
@SuppressWarnings("rawtypes")
public class DynamicLinearPipeline implements ICapability, Serializable{
	// TODO handle concurrent modifications to capability bindings
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final String description;
	private final List<Object> capabilities;

	public DynamicLinearPipeline(String name, String description, List<Object> capabilities) {
		super();
		this.name = name;
		this.description = description;
		this.capabilities = Lists.newArrayList(capabilities);
	}

	private List<ICapability<?,?>> current() throws NoSuchCapabilityException{
		List<ICapability<?,?>> result = Lists.newArrayList();
		for (Object capability : capabilities){
			if (capability instanceof ICapability){
				result.add((ICapability) capability);
			}else if (capability instanceof String){
				result.add(CupidPlatform.getCapabilityRegistry().findCapability((String) capability));
			}else{
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
		}
		return result;
	}
	
	@Override
	public String getUniqueId() {
		StringBuilder builder = new StringBuilder();
		builder.append("[pipe:");
		for (Object capability : capabilities){
			if (capability instanceof ICapability){
				builder.append(((ICapability) capability).getUniqueId());
			}else if (capability instanceof String){
				builder.append(((String)capability) + ";");
			}else{
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
			
			builder.append(capability + ";");
		}
		builder.append("]");
		return builder.toString();	
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public TypeToken getParameterType() {
		try {
			return current().get(0).getParameterType();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}

	@Override
	public TypeToken getReturnType() {
		try {
			return current().get(capabilities.size()-1).getReturnType();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CapabilityJob getJob(Object input) {
		return new CapabilityJob(this, input){
			@Override
			protected CapabilityStatus run(IProgressMonitor monitor) {
				Object result = getInput();
				
				monitor.beginTask(this.getName(), DynamicLinearPipeline.this.capabilities.size());

				List<ICapability<?, ?>> capabilities;
				try {
					capabilities = current();
				} catch (NoSuchCapabilityException e) {
					return CapabilityStatus.makeError(e);
				}
				
				List<Object> intermediateResults = Lists.newArrayList();
				intermediateResults.add(result);
				
				try{

					for (ICapability capability : capabilities){
						if (monitor.isCanceled()){
							return CapabilityStatus.makeCancelled();
						}

						CapabilityJob subtask = capability.getJob(result);
						monitor.subTask(subtask.getName());
						subtask.schedule();
						try {
							subtask.join();
						} catch (InterruptedException e) {
							monitor.done();
							return CapabilityStatus.makeError(e);
						}
						
						CapabilityStatus status = ((CapabilityStatus)subtask.getResult());
						
						if (status.getCode() == Status.OK){
							result = status.value();
							intermediateResults.add(result);
							monitor.worked(1);
						}else{
							monitor.done();
							return CapabilityStatus.makeError(status.getException());
						}
					}
				}catch(Exception e){
					monitor.done();
					return CapabilityStatus.makeError(e);
				}
				
				monitor.done();
				return CapabilityStatus.makeOk(result);
			}
		};
	}

	@Override
	public boolean isPure() {
		try {
			return Iterables.all(current(), new Predicate<ICapability<?,?>>(){
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
			return Iterables.all(current(), new Predicate<ICapability<?,?>>(){
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
			return Iterables.any(current(), new Predicate<ICapability<?,?>>(){
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
