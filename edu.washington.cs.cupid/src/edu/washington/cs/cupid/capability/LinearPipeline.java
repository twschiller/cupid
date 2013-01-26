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
package edu.washington.cs.cupid.capability;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

/**
 * Executes a series of static capabilities sequentially.
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> input type
 * @param <T> output type
 */
public final class LinearPipeline<I, T> implements ICapability<I, T> {

	// TODO support caching for intermediary values
	
	private final String name;
	private final String description;
	
	@SuppressWarnings("rawtypes")
	private final List<ICapability> capabilities;
	
	private final boolean pure;
	private final boolean trans;
	
	/**
	 * Construct a pipeline of capabilities.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities the capabilities in the pipeline
	 */
	private LinearPipeline(final String name, final String description, @SuppressWarnings("rawtypes") final ICapability[] capabilities) {
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
	public CapabilityJob<I, T> getJob(final I input) {
		return new CapabilityJob(this, input) {
			@Override
			protected CapabilityStatus<T> run(final IProgressMonitor monitor) {
				try {
					Object result = getInput();
					
					monitor.beginTask(LinearPipeline.this.getName(), LinearPipeline.this.capabilities.size());

					for (ICapability capability : capabilities) {
						if (monitor.isCanceled()) {
							return CapabilityStatus.makeCancelled();
						}
						
						CapabilityJob subtask = capability.getJob(result);
						
						subtask.setProgressGroup(new SubProgressMonitor(monitor, 1), 1);
						subtask.schedule();
						subtask.join();
						
						CapabilityStatus status = (CapabilityStatus) subtask.getResult();
						
						if (status.getCode() == Status.OK) {
							result = status.value();
						} else {
							throw status.getException();
						}		
					}
					return CapabilityStatus.makeOk((T) result);
				} catch (Throwable ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
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
		return capabilities.get(capabilities.size() - 1).getReturnType();
	}

	@Override
	public boolean isPure() {
		return pure;
	}
	
	@Override
	public boolean isLocal() {
		for (ICapability<?, ?> capability : capabilities) {
			if (!capability.isLocal()) {
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
	public static class PipelineBuilder<X, Y> {
		@SuppressWarnings("rawtypes")
		private final List<ICapability> capabilities;
		
		/**
		 * An initial pipeline builder with capability <code>first</code>.
		 * @param first the first capability in the pipeline
		 */
		public PipelineBuilder(final ICapability<X, Y> first) {
			capabilities = Lists.newArrayList();
			capabilities.add(first);
		}
		
		/**
		 * Attach a capability to the end of the pipeline.
		 * @param next the capability to attach
		 * @param <Z> the intermediate type
		 * @return the pipeline builder with <code>next</code> attached
		 */
		public final <Z> PipelineBuilder<X, Y> attach(final ICapability<Z, Y> next) {
			capabilities.add(next);
			return this;
		}
		
		/**
		 * Returns a capability for the constructed pipeline.
		 * @param name the pipeline name
		 * @param description the pipeline description
		 * @return a capability for the constructed pipeline.
		 */
		public final LinearPipeline<X, Y> create(final String name, final String description) {
			return new LinearPipeline<X, Y>(name, description, capabilities.toArray(new ICapability[]{}));
		}
	}

	@Override
	public Set<String> getDynamicDependencies() {
		return new HashSet<String>();
	}
}
