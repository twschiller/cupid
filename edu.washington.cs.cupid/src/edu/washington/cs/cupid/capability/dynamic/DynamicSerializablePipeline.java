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
package edu.washington.cs.cupid.capability.dynamic;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.capability.linear.ILinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * A linear pipeline with dynamic binding that can be serialized.
 * @param <I> input type
 * @param <V> output type
 * @author Todd Schiller
 */
public class DynamicSerializablePipeline<I, V> extends AbstractDynamicSerializableCapability implements ILinearCapability<I, V> {
	// TODO handle concurrent modifications to capability bindings
	
	private static final long serialVersionUID = 1L;
	
	private final List<Serializable> capabilities;
	
	/**
	 * Construct a serializable pipeline of capabilities.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities the pipeline of capabilities
	 */
	public DynamicSerializablePipeline(final String name, final String description, final List<Serializable> capabilities) {
		super(name, description, capabilities);
		this.capabilities = Lists.newArrayList(capabilities);
	}
	
	private List<ILinearCapability<?, ?>> inorder() throws NoSuchCapabilityException {
		Map<String, ICapability> map = super.current();
		
		List<ILinearCapability<?, ?>> result = Lists.newArrayList();
	
		for (Object capability : capabilities) {
			if (capability instanceof ILinearCapability) {
				result.add((ILinearCapability<?, ?>) capability);
			} else if (capability instanceof String) {
				result.add((ILinearCapability<?, ?>) map.get((String) capability));
			} else {
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
		}
		return result;
	}
	
	@Override
	public final String getUniqueId() {
		StringBuilder builder = new StringBuilder();
		builder.append("[pipe:");
		for (Object capability : capabilities) {
			if (capability instanceof ICapability) {
				builder.append(((ICapability) capability).getUniqueId());
			} else if (capability instanceof String) {
				builder.append(((String) capability));
			} else {
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
			
			builder.append(";");
		}
		builder.append("]");
		return builder.toString();	
	}

	@Override
	public LinearJob<I, V> getJob(ICapabilityArguments input) {
		return getJob(input.getValueArgument(getParameter()));
	}

	@Override
	public final LinearJob<I, V> getJob(final I input) {
		return new LinearJob<I, V>(this, input) {
			@Override
			protected LinearStatus<V> run(final IProgressMonitor monitor) {
				try {

					Object result = getInput();

					monitor.beginTask(this.getName(), DynamicSerializablePipeline.this.capabilities.size());

					List<ILinearCapability<?, ?>> resolved = inorder();

					List<Object> intermediateResults = Lists.newArrayList();
					intermediateResults.add(result);

					for (ILinearCapability capability : resolved) {
						if (monitor.isCanceled()) {
							return LinearStatus.<V>makeCancelled();
						}

						LinearJob<?, ?> subtask = capability.getJob(result);

						if (subtask == null) {
							throw new RuntimeException("Capability " + capability.getName() + " produced null job");
						}

						subtask.setProgressGroup(new SubProgressMonitor(monitor, 1), 1);
						subtask.schedule();
						subtask.join();

						LinearStatus<?> status = ((LinearStatus<?>) subtask.getResult());

						if (status.getCode() == Status.OK) {
							result = status.getOutputValue();
							intermediateResults.add(result);
						} else {
							throw status.getException();
						}
					}
					return LinearStatus.<V>makeOk(getCapability(), (V) result);
				} catch (Throwable ex) {
					return LinearStatus.<V>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

	@Override
	public IParameter<I> getParameter() {
		try {
			return (IParameter<I>) inorder().get(0).getParameter();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}

	@Override
	public Set<IParameter<?>> getParameters() {
		return Collections.<IParameter<?>>singleton(getParameter());
	}
	
	@Override
	public IOutput<V> getOutput() {
		try {
			List<ILinearCapability<?, ?>> ordered = inorder();
			return (IOutput<V>) ordered.get(ordered.size()-1).getOutput();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}	
	}


	@Override
	public Set<IOutput<?>> getOutputs() {
		return Collections.<IOutput<?>>singleton(getOutput());
	}

}
