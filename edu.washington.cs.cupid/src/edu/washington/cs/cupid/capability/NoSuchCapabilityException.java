package edu.washington.cs.cupid.capability;

/**
 * Thrown to indicate that a capability is not available.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class NoSuchCapabilityException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final String capabilityId;

	/**
	 * Construct an exception indicating that <code>capabilityId</code> is not available.
	 * @param capabilityId the id of the capability
	 */
	public NoSuchCapabilityException(final String capabilityId) {
		super();
		this.capabilityId = capabilityId;
	}

	/**
	 * Returns the id of the missing capability.
	 * @return the id of the missing capability
	 */
	public final String getCapabilityId() {
		return capabilityId;
	}
}
