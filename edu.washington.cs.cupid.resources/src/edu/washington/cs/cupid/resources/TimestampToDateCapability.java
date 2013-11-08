package edu.washington.cs.cupid.resources;

import java.util.Date;

import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;

public class TimestampToDateCapability extends LinearCapability<Long, Date> {
	/**
	 * Construct a capability that returns the project that contains a resource.
	 */
	public TimestampToDateCapability() {
		super(
				"Timestamp to Date (Convert)",
				"Convert a timestamp to a date",
				Long.class, Date.class,
				Flag.PURE);
	}
	
	@Override
	public LinearJob<Long, Date> getJob(Long input) {
		return new ImmediateJob<Long, Date>(this, input, new Date(input));
	}
}
