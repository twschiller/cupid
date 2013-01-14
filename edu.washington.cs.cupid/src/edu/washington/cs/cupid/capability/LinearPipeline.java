package edu.washington.cs.cupid.capability;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

/**
 * Executes a series of static capabilities sequentially.
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> input type
 * @param <T> output type
 */
public class LinearPipeline<I,T> implements ICapability<I,T>{

	// TODO support caching for intermediary values
	
	private final String name;
	private final String description;
	
	@SuppressWarnings("rawtypes")
	protected final List<ICapability> capabilities;
	
	private final boolean pure;
	private final boolean trans;
	
	public <S> LinearPipeline(String name, String description, ICapability<I,S> first, ICapability<S,T> second){
		this(name, description, new ICapability[]{first, second});
	}
	
	public <S,Q> LinearPipeline(String name, String description, ICapability<I,S> first, ICapability<S,Q> second, ICapability<Q,T> third){
		this(name, description, new ICapability[]{first, second, third});
	}
	
	/**
	 * @deprecated not type safe, use {@link PipelineBuilder}
	 */
	public LinearPipeline(String name, String description, @SuppressWarnings("rawtypes") ICapability[] capabilities){
		this.name = name;
		this.description = description;
		this.capabilities = Lists.newArrayList(capabilities);
		
		boolean lpure = true;
		boolean ltrans = false;
		
		for (ICapability<?,?> capability : capabilities){
			if (!capability.getDynamicDependencies().isEmpty()){
				throw new IllegalArgumentException("Static pipelines cannot have dynamic dependencies: " + capability.getUniqueId());
			}
			
			lpure &= capability.isPure();
			ltrans |= capability.isTransient();
		}
		
		this.pure = lpure;
		this.trans = ltrans;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CapabilityJob<I, T> getJob(I input) {
		return new CapabilityJob(this, input){
			@Override
			protected CapabilityStatus<T> run(IProgressMonitor monitor) {
				Object result = getInput();
				
				monitor.beginTask(this.getName(), LinearPipeline.this.capabilities.size());

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
					result = ((CapabilityStatus)subtask.getResult()).value();
					monitor.worked(1);
				}
				
				monitor.done();
				return CapabilityStatus.makeOk((T) result);
			}
		};
	}

	@Override
	public boolean isTransient() {
		return trans;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<I> getParameterType() {
		return capabilities.get(0).getParameterType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<T> getReturnType() {
		return capabilities.get(capabilities.size()-1).getReturnType();
	}

	@Override
	public boolean isPure() {
		return pure;
	}
	
	@Override
	public boolean isLocal() {
		for (ICapability<?,?> capability : capabilities){
			if (!capability.isLocal()){
				return false;
			}
		}
		return true;
	}


	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUniqueId() {
		StringBuilder builder = new StringBuilder();
		builder.append("[pipe:");
		for (ICapability<?,?> capability : capabilities){
			builder.append(capability.getUniqueId() + ";");
		}
		builder.append("]");
		return builder.toString();	
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class PipelineBuilder<X,Y> {
		private final List<ICapability> capabilities;
		
		public PipelineBuilder(ICapability<X,Y> first){
			this.capabilities = Lists.newArrayList( (ICapability) first);
		}
		
		private PipelineBuilder(List<ICapability> capabilities, ICapability next){
			this.capabilities = Lists.newArrayList();
			this.capabilities.addAll(capabilities);
			this.capabilities.add(next);
		}
		
		
		public <Z> PipelineBuilder<X,Y> attach(ICapability<Z, Y> next){
			return new PipelineBuilder(this.capabilities, next);
		}
		
		public LinearPipeline<X,Y> create(String name, String description){
			return new LinearPipeline(name, description, this.capabilities.toArray(new ICapability[]{}));
		}
	}

	@Override
	public Set<String> getDynamicDependencies() {
		return new HashSet<String>();
	}
}
