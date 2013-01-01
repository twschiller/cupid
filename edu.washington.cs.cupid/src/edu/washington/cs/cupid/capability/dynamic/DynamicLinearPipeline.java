package edu.washington.cs.cupid.capability.dynamic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;

/**
 * A linear pipeline with dynamic binding
 * @author Todd Schiller
 */
@SuppressWarnings("rawtypes")
public class DynamicLinearPipeline<I,V> extends DynamicPipeline<I,V>{
	// TODO handle concurrent modifications to capability bindings
	
	private static final long serialVersionUID = 1L;
	
	private final List<Serializable> capabilities;

	public DynamicLinearPipeline(String name, String description, List<Serializable> capabilities) {
		super(name, description, capabilities);
		this.capabilities = Lists.newArrayList(capabilities);
	}

	private List<ICapability<?,?>> inorder() throws NoSuchCapabilityException{
		Map<String, ICapability<?,?>> map = super.current();
		
		List<ICapability<?,?>> result = Lists.newArrayList();
	
		for (Object capability : capabilities){
			if (capability instanceof ICapability){
				result.add((ICapability) capability);
			}else if (capability instanceof String){
				result.add(map.get((String) capability));
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
				builder.append(((String)capability));
			}else{
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
			
			builder.append(";");
		}
		builder.append("]");
		return builder.toString();	
	}

	private ICapability<?,?> get(int index) throws NoSuchCapabilityException{
		return get(capabilities.get(index));
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<I> getParameterType() {
		try {
			return (TypeToken<I>) get(0).getParameterType();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<V> getReturnType() {
		try {
			return (TypeToken<V>) get(capabilities.size()-1).getReturnType();
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
					capabilities = inorder();
				} catch (NoSuchCapabilityException e) {
					return CapabilityStatus.makeError(e);
				}
				
				List<Object> intermediateResults = Lists.newArrayList();
				intermediateResults.add(result);

				for (ICapability capability : capabilities){
					if (monitor.isCanceled()){
						return CapabilityStatus.makeCancelled();
					}

					CapabilityJob<?,?> subtask = capability.getJob(result);
					
					if (subtask == null){
						throw new RuntimeException("Capability " + capability.getName() + " produced null job");
					}
					
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

				monitor.done();
				return CapabilityStatus.makeOk(result);
			}
		};
	}
}
