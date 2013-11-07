package edu.washington.cs.cupid.standard;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class Any extends LinearCapability<Collection<Boolean>, Boolean> {

	/**
	 * A capability that indicates whether any of the elements in the collection are true
	 */
	public Any() {
		super("Any (One or More)", 
			  "True if any of the elements are true",
			  new TypeToken<Collection<Boolean>>(){},
			  TypeToken.of(Boolean.class),
			  Flag.PURE);
	}

	@Override
	public LinearJob<Collection<Boolean>, Boolean> getJob(Collection<Boolean> input) {
		return new LinearJob<Collection<Boolean>, Boolean> (this, input) {
			@Override
			protected LinearStatus<Boolean> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), getInput().size());
					for (Boolean elt : getInput()){
						if (elt) return LinearStatus.makeOk(Any.this, true);
						monitor.worked(1);
					}
					return LinearStatus.makeOk(Any.this, false);
				} catch (Exception ex) {
					return LinearStatus.<Boolean>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
	
}
