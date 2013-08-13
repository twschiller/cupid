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
package edu.washington.cs.cupid.jobs;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

/**
 * A default job change listener implementation suitable for subclassing. The default implementations of the other methods do nothing.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class NullJobListener implements IJobChangeListener {

	@Override
	public void aboutToRun(final IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void awake(final IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void done(final IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void running(final IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void scheduled(final IJobChangeEvent event) {
		// NO OP
	}

	@Override
	public void sleeping(final IJobChangeEvent event) {
		// NO OP
	}

}
