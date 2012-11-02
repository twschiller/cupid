package edu.washington.cs.cupid.jobs;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;

/**
 * A capability job that immediately returns
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> input type
 * @param <V> output type
 */
public final class ImmediateJob<I,V> extends CapabilityJob<I,V>{
	
	private final V value;
	private final Throwable exception;
	
	public ImmediateJob(ICapability<I, V> capability, I input, V value) {
		super(capability, input);
		
		if (value == null){
			throw new IllegalArgumentException("Jobs cannot return null");
		}
		
		this.value = value;
		this.exception = null;
	}
	
	public ImmediateJob(ICapability<I, V> capability, I input, Throwable exception) {
		super(capability, input);
		
		if (exception == null){
			throw new IllegalArgumentException("Exceptional jobs cannot have a null exception");
		}
		
		this.value = null;
		this.exception = exception;
	}

	@Override
	protected CapabilityStatus<V> run(IProgressMonitor monitor) {
		monitor.done();
		
		return value != null ? 
				CapabilityStatus.makeOk(value) 
				: CapabilityStatus.<V>makeError(exception);
	}
}
