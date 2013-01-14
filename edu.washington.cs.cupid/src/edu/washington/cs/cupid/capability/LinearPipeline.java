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
public class LinearPipeline<I, T> implements ICapability<I, T> {

	// TODO support caching for intermediary values
	
	private final String name;
	private final String description;
	
	/**
	 * The capabilities in the pipeline.
	 */
	@SuppressWarnings("rawtypes")
	protected final List<ICapability> capabilities;
	
	private final boolean pure;
	private final boolean trans;
	
	/**
	 * Construct a two-capability pipeline.
	 * @param name the capability name
	 * @param description the capability description
	 * @param first the first capability in the pipeline
	 * @param second the second capability in the pipeline
	 * @param <S> the intermediate type
	 */
	public <S> LinearPipeline(final String name, final String description, final ICapability<I,S> first, final ICapability<S,T> second){
		this(name, description, new ICapability[]{first, second});
	}
	
	/**
	 * Construct a three-capability pipeline.
	 * @param name the capability name
	 * @param description the capability description
	 * @param first the first capability in the pipeline
	 * @param second the second capability in the pipeline
	 * @param <S> the first intermediate type
	 * @param <Q> the second intermediate type
	 */
	public <S,Q> LinearPipeline(final String name, final String description, final ICapability<I,S> first, final ICapability<S,Q> second, final ICapability<Q,T> third){
		this(name, description, new ICapability[]{first, second, third});
	}
	
	/**
	 * Construct a pipeline of capabilities.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities the capabilities in the pipeline
	 * @deprecated not type safe, use {@link PipelineBuilder}
	 */
	public LinearPipeline(final String name, final String description, @SuppressWarnings("rawtypes") final ICapability[] capabilities) {
		this.name = name;
		this.description = description;
		this.capabilities = Lists.newArrayList(capabilities);
		
		boolean lpure = true;
		boolean ltrans = false;
		
		for (ICapability<?, ?> capability : capabilities) {
			if (!capability.getDynamicDependencies().isEmpty()) {
				throw new IllegalArgumentException("Static pipelines cannot have dynamic dependencies: " + capability.getUniqueId());
			}
			
			lpure &= capability.isPure();
			ltrans |= capability.isTransient();
		}
		
		this.pure = lpure;
		this.trans = ltrans;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final CapabilityJob<I, T> getJob(final I input) {
		return new CapabilityJob(this, input) {
			@Override
			protected CapabilityStatus<T> run(final IProgressMonitor monitor) {
				Object result = getInput();
				
				monitor.beginTask(LinearPipeline.this.getName(), LinearPipeline.this.capabilities.size());

				for (ICapability capability : capabilities) {
					if (monitor.isCanceled()) {
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
					result = ((CapabilityStatus) subtask.getResult()).value();
					monitor.worked(1);
				}
				
				monitor.done();
				return CapabilityStatus.makeOk((T) result);
			}
		};
	}

	@Override
	public final boolean isTransient() {
		return trans;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final TypeToken<I> getParameterType() {
		return capabilities.get(0).getParameterType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final TypeToken<T> getReturnType() {
		return capabilities.get(capabilities.size() - 1).getReturnType();
	}

	@Override
	public final boolean isPure() {
		return pure;
	}
	
	@Override
	public final boolean isLocal() {
		for (ICapability<?, ?> capability : capabilities) {
			if (!capability.isLocal()) {
				return false;
			}
		}
		return true;
	}


	@Override
	public final String getDescription() {
		return description;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String getUniqueId() {
		StringBuilder builder = new StringBuilder();
		builder.append("[pipe:");
		for (ICapability<?, ?> capability : capabilities) { 
			builder.append(capability.getUniqueId() + ";");
		}
		builder.append("]");
		return builder.toString();	
	}
	
	/**
	 * A fluent interface for building pipelines.
	 * @author Todd Schiller
	 * @param <X> the pipeline input type
	 * @param <Y> the pipeline output type
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	public static class PipelineBuilder<X, Y> {
		private final List<ICapability> capabilities;
		
		/**
		 * An initial pipeline builder with capability <code>first</code>.
		 * @param first the first capability in the pipeline
		 */
		public PipelineBuilder(final ICapability<X, Y> first) {
			this.capabilities = Lists.newArrayList((ICapability) first);
		}
		
		private PipelineBuilder(final List<ICapability> capabilities, final ICapability next) {
			this.capabilities = Lists.newArrayList();
			this.capabilities.addAll(capabilities);
			this.capabilities.add(next);
		}
		
		/**
		 * Attach a capability to the end of the pipeline.
		 * @param next the capability to attach
		 * @param <Z> the intermediate type
		 * @return the pipeline builder with <code>next</code> attached
		 */
		public final <Z> PipelineBuilder<X, Y> attach(final ICapability<Z, Y> next) {
			return new PipelineBuilder(this.capabilities, next);
		}
		
		/**
		 * Returns a capability for the constructed pipeline.
		 * @param name the pipeline name
		 * @param description the pipeline description
		 * @return a capability for the constructed pipeline.
		 */
		public final LinearPipeline<X, Y> create(final String name, final String description) {
			return new LinearPipeline(name, description, this.capabilities.toArray(new ICapability[]{}));
		}
	}

	@Override
	public final Set<String> getDynamicDependencies() {
		return new HashSet<String>();
	}
}
