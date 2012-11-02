package edu.washington.cs.cupid.capability;

/**
 * Thrown to indicate that a capability is malformed (e.g., a job could not be created).
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class MalformedCapabilityException extends Exception{

	private final ICapability<?,?> capability;
	
	private static final long serialVersionUID = 1L;

	public MalformedCapabilityException(ICapability<?,?> capability, String msg){
		super(msg);
		this.capability = capability;
	}
	
	public MalformedCapabilityException(ICapability<?,?> capability, Throwable internal){
		super(internal);
		this.capability = capability;
	}

	public ICapability<?,?> getCapability(){
		return capability;
	}
}
