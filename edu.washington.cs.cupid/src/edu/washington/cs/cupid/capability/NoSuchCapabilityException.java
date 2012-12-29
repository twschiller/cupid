package edu.washington.cs.cupid.capability;

/**
 * Thrown to indicate that a capability is not available.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class NoSuchCapabilityException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private final String capabilityId;

	/**
	 * Constructs a <code>NoSuchCapabilityException</code> for the specified capability.
	 * @param capabilityId the id of the capability
	 */
	public NoSuchCapabilityException(String capabilityId) {
		super();
		this.capabilityId = capabilityId;
	}

	/**
	 * @return the id of the capability
	 */
	public String getCapabilityId() {
		return capabilityId;
	}
}
