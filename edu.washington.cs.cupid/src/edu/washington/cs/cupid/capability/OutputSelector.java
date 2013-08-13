package edu.washington.cs.cupid.capability;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.dynamic.DynamicBindingException;
import edu.washington.cs.cupid.capability.exception.MalformedCapabilityException;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;

public class OutputSelector extends AbstractSerializableCapability{

	private static final long serialVersionUID = 1L;

	private Serializable capability;
	private String outputName;
	
	public OutputSelector(ICapability capability, IOutput<?> output) {
		super("Output '" + output + "' of '" + capability + "'", 
			  "Output '" + output + "' of '" + capability + "'");
		
		if (CapabilityUtil.isSerializable(capability)){
			this.capability = (Serializable) capability;
		}else{
			this.capability = capability.getName();
		}
		
		this.outputName = output.getName();
	}
	
	public ICapability getCapability() throws NoSuchCapabilityException{
		if (capability instanceof ICapability) {
			return (ICapability) capability;
		} else if (capability instanceof String) {
			return CupidPlatform.getCapabilityRegistry().findCapability((String) capability);
		} else {
			throw new RuntimeException("Unexpected capability descriptior of type " + capability.getClass().getName());
		}
	}
	
	public ICapability.IOutput<?> getOutput() throws NoSuchCapabilityException{
		return CapabilityUtil.findOutput(getCapability(), outputName);
	}
	
	@Override
	public Set<? extends IParameter<?>> getParameters() {
		try {
			return getCapability().getParameters();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}

	@Override
	public Set<? extends IOutput<?>> getOutputs() {
		ICapability c;
		Set<? extends IOutput<?>> os = null;
		try {
			c = getCapability();
			
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
		
		os = c.getOutputs();
		for (IOutput<?> o : os){
			if (o.getName().equals(outputName)){
				Set<IOutput<?>> result = Sets.<IOutput<?>>newHashSet(o);
				return result;
			}
		}
		
		throw new DynamicBindingException(new MalformedCapabilityException(c, "No output '" + outputName + "' exists for capability"));
	}

	@Override
	public CapabilityJob<? extends ICapability> getJob(ICapabilityArguments input) {
		ICapability c = null;
		try {
			c = getCapability();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
		
		final ICapability capability = c;
		
		return new CapabilityJob<ICapability>(this, input) {
			@Override
			protected CapabilityStatus run(IProgressMonitor monitor) {
				monitor.beginTask(this.getName(), 2);
				
				try{
					CapabilityJob<?> subtask = capability.getJob(getInputs());

					if (subtask == null) {
						throw new RuntimeException("Capability " + capability.getName() + " produced null job");
					}

					subtask.setProgressGroup(new SubProgressMonitor(monitor, 1), 1);
					subtask.schedule();
					subtask.join();

					CapabilityStatus status = (CapabilityStatus) subtask.getResult();

					if (status.getCode() == Status.OK) {
						Object result = status.value().getOutput(outputName);
						return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(OutputSelector.this, result));
					} else {
						throw status.getException();
					}
					
				} catch (Throwable e) {
					return CapabilityStatus.makeError(e);
				}finally{
					monitor.done();
				}
			}
		};
	}

	@Override
	public EnumSet<Flag> getFlags() {
		try {
			return getCapability().getFlags();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}
}
