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
package edu.washington.cs.cupid.capability.linear;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityInput;
import edu.washington.cs.cupid.capability.IDynamicCapability;

/**
 * Executes a series of static capabilities sequentially.
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> input type
 * @param <T> output type
 */
@SuppressWarnings("rawtypes")
public final class LinearPipeline implements ILinearCapability {

	// TODO support caching for intermediary values
	
	private final String name;
	private final String description;
	private final List<ILinearCapability> capabilities;
	private final EnumSet<Flag> flags;
	
	/**
	 * Construct a pipeline of capabilities.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities the capabilities in the pipeline
	 */
	public LinearPipeline(final String name, final String description, final ILinearCapability... capabilities) {
		this.name = name;
		this.description = description;
		this.capabilities = Lists.newArrayList(capabilities);
		
		flags = EnumSet.of(Flag.PURE);
		
		for (ILinearCapability capability : capabilities) {
			if (capability instanceof IDynamicCapability) {
				throw new IllegalArgumentException("Static pipelines cannot have dynamic dependencies: " + capability.getUniqueId());
			}
			
			if (!capability.getFlags().contains(Flag.PURE)){
				flags.remove(Flag.PURE);
			}
			
			if (capability.getFlags().contains(Flag.TRANSIENT)){
				flags.add(Flag.TRANSIENT);
			}

		}
	}

	@Override
	public LinearJob getJob(Object input) {
		return new LinearJob(this, input) {
			@Override
			protected LinearStatus run(final IProgressMonitor monitor) {
				try {
					Object result = getInput();
					
					monitor.beginTask(LinearPipeline.this.getName(), LinearPipeline.this.capabilities.size());

					for (ILinearCapability capability : capabilities) {
						if (monitor.isCanceled()) {
							return LinearStatus.makeCancelled();
						}
						
						LinearJob subtask = capability.getJob(result);
						
						subtask.setProgressGroup(new SubProgressMonitor(monitor, 1), 1);
						subtask.schedule();
						subtask.join();
						
						LinearStatus status = (LinearStatus) subtask.getResult();
						
						if (status.getCode() == Status.OK) {
							result = status.value();
						} else {
							throw status.getException();
						}		
					}
					return LinearStatus.makeOk(result);
				} catch (Throwable ex) {
					return LinearStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}	
	
	@Override
	public LinearJob getJob(final ICapabilityInput input) {
		return getJob(input.getArguments().get(this.capabilities.get(0).getParameters()));
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
		for (ICapability capability : capabilities) { 
			builder.append(capability.getUniqueId() + ";");
		}
		builder.append("]");
		return builder.toString();	
	}
	
	@Override
	public EnumSet<Flag> getFlags() {
		return flags;
	}

	@Override
	public Parameter<?> getParameter() {
		return capabilities.get(0).getParameter();
	}

	@Override
	public Output<?> getOutput() {
		return capabilities.get(capabilities.size()-1).getOutput();
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return (Set<Parameter<?>>) capabilities.get(0).getParameters();
	}

	@Override
	public Set<Output<?>> getOutputs() {
		return (Set<Output<?>>) capabilities.get(capabilities.size()-1).getOutputs();
	}
	
}
