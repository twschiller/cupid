package edu.washington.cs.cupid.jobs;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

/**
 * A default job change listener implementation suitable for subclassing. The default implementations of the other methods do nothing.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class NullJobListener implements IJobChangeListener{

	@Override
	public void aboutToRun(IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void awake(IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void done(IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void running(IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void sleeping(IJobChangeEvent event) {
		// NO OP
	}

}
