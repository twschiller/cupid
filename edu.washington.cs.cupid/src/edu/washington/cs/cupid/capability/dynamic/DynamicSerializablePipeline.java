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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.capability.CapabilityArguments;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.OptionalParameter;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.Parameter;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;

/**
 * A linear pipeline with dynamic binding that can be serialized.
 * @param <I> input type
 * @param <V> output type
 * @author Todd Schiller
 */
public class DynamicSerializablePipeline extends AbstractDynamicSerializableCapability {
	// TODO handle concurrent modifications to capability bindings
	
	private static final long serialVersionUID = 20130729L;
	
	private final List<Serializable> capabilities;
	private final List<Map<IParameter<?>, Serializable>> sources;
	private final Set<IParameter<?>> parameters;
	private final IOutput<?> output;
	
	/**
	 * Construct a serializable pipeline of capabilities.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities the pipeline of capabilities
	 */
	public DynamicSerializablePipeline(
			final String name, 
			final String description, 
			final List<Serializable> capabilities,
			final List<? extends ICapabilityArguments> options){
		
		super(name, description, capabilities);
		this.capabilities = Lists.newArrayList(capabilities);
		
		sources = Lists.newArrayList();
		parameters = Sets.newHashSet();
		
		try {
			List<ICapability> bind = inorder();
	
			// create options
			
			for (int i = 0; i < bind.size(); i++){
				ICapability capability = bind.get(i);
				ICapabilityArguments capabilityOptions = options.get(i);
				
				Map<IParameter<?>, Serializable> capabilitySources = Maps.newHashMap();
				
				for (IParameter<?> option : CapabilityUtil.options(capability)){
					
					if (capabilityOptions.hasValueArgument(option)){
						IParameter<?> copy = new OptionalParameter(option.getName(), option.getType(), (Serializable) capabilityOptions.getValueArgument(option));
						parameters.add(copy);
						capabilitySources.put(option, copy);
					}else{
						ICapability optionCapability = capabilityOptions.getCapabilityArgument(option);
						capabilitySources.put(option, (Serializable) optionCapability);	
					}
				}
				
				sources.add(capabilitySources);
			}
			
			// create initial parameter
			
			ICapability first = bind.get(0);
			if (CapabilityUtil.isUnary(first)){
				IParameter<?> input = CapabilityUtil.unaryParameter(first);
				IParameter<?> copy = new Parameter(input.getName(), input.getType());
				parameters.add(CapabilityUtil.unaryParameter(first));
				sources.get(0).put(input, copy);
			}
			
			output = calculateOutputType(bind);
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}	
	}
	
	
	

	
	/**
	 * Compute the output type of a dynamic capability. If the output type of the last capability
	 * includes a type variable, try to resolve the type variable.
	 * @param pipe
	 * @return
	 */
	private static IOutput<?> calculateOutputType(List<ICapability> pipe){
		ICapability last = pipe.get(pipe.size()-1);
		IOutput<?> lastOut = CapabilityUtil.singleOutput(last);
		return new Output(lastOut.getName(), PipelineUtil.calculateOutputType(pipe));
	}
	
	private List<ICapability> inorder() throws NoSuchCapabilityException {
		Map<String, ICapability> map = super.current();
		
		List<ICapability> result = Lists.newArrayList();
	
		for (Object capability : capabilities) {
			if (capability instanceof ICapability) {
				result.add((ICapability) capability);
			} else if (capability instanceof String) {
				result.add((ICapability) map.get((String) capability));
			} else {
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
		}
		return result;
	}
	
	private ICapabilityArguments formArguments(
			final Object mainInput, 
			final ICapability capability, 
			final Map<IParameter<?>, Serializable> capabilitySources, 
			final ICapabilityArguments options, 
			final Object input){
		
		CapabilityArguments args = new CapabilityArguments();
		
		if (CapabilityUtil.isUnary(capability)){
			args.add((IParameter) CapabilityUtil.unaryParameter(capability), input);	
		}
		
		for (final IParameter option : CapabilityUtil.options(capability)){
			Serializable source = capabilitySources.get(option);
			
			if (source instanceof ICapability){
				ICapability optionCapability = (ICapability) source;
				CapabilityJob<?> job = optionCapability.getJob(CapabilityUtil.packUnaryInput(optionCapability, mainInput));
				
				job.schedule();
				try {
					job.join();
				} catch (InterruptedException e) {
					throw new RuntimeException("Error running option capability " + optionCapability.getName(), e);
				}
				
				CapabilityStatus status = (CapabilityStatus) job.getResult();

				if (status.getCode() == Status.OK) {
					args.add(option, CapabilityUtil.singleOutputValue(optionCapability, status));
				} else {
					throw new RuntimeException("Error running option capability " + optionCapability.getName(), status.getException());
				}
				
			}else if (source instanceof IParameter){
				args.add(option, options.getValueArgument((IParameter<?>) source));		
			}else{
				throw new IllegalArgumentException("Unexpected argument of type " + source.getClass().getName());
			}
		}
		
		return args;
	}
	
	@Override
	public CapabilityJob<ICapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<ICapability>(this, input) {
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(this.getName(), DynamicSerializablePipeline.this.capabilities.size());

					List<ICapability> resolved = inorder();
					
					final Object mainInput = CapabilityUtil.isGenerator(getCapability()) ?
							null :
							getInputs().getValueArgument(CapabilityUtil.unaryParameter(getCapability()));
					
					Object result = mainInput;
					List<Object> intermediateResults = Lists.newArrayList();
					intermediateResults.add(result);

					for (int i = 0; i < resolved.size(); i++){
						ICapability capability = resolved.get(i);
						Map<IParameter<?>, Serializable> capabilitySources = sources.get(i);
						
						if (monitor.isCanceled()) {
							return CapabilityStatus.makeCancelled();
						}

						CapabilityJob<?> subtask = capability.getJob(formArguments(mainInput, capability, capabilitySources, getInputs(), result));

						if (subtask == null) {
							throw new RuntimeException("Capability " + capability.getName() + " produced null job");
						}

						subtask.setProgressGroup(new SubProgressMonitor(monitor, 1), 1);
						subtask.schedule();
						subtask.join();

						CapabilityStatus status = (CapabilityStatus) subtask.getResult();

						if (status.getCode() == Status.OK) {
							result = CapabilityUtil.singleOutputValue(capability, status);
							intermediateResults.add(result);
						} else {
							throw status.getException();
						}
					}
					return CapabilityStatus.makeOk(
							CapabilityUtil.packSingleOutputValue(DynamicSerializablePipeline.this, 
							result));
				} catch (Throwable ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

	@Override
	public Set<IParameter<?>> getParameters() {
		return parameters;
	
	}
	
	@Override
	public Set<IOutput<?>> getOutputs() {
		Set<IOutput<?>> result = Sets.newHashSet();
		result.add(output);
		return result;
	}

}
