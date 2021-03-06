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
package edu.washington.cs.cupid.capability.linear;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * A capability job that immediately returns.
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> input type
 * @param <V> output type
 */
public final class ImmediateJob<I, V> extends LinearJob<I, V> {
	
	private final V value;
	private final Throwable exception;
	
	/**
	 * A job that immediately returns with <code>exception</code>.
	 * @param capability the source of the job
	 * @param input the job input
	 * @param throwable the exception to immediately return with
	 */
	public ImmediateJob(final ILinearCapability<I, V> capability, final I input, final Throwable throwable) {
		super(capability, input);
		
		if (throwable == null) {
			throw new IllegalArgumentException("Exceptional jobs cannot have a null exception");
		}
		
		this.value = null;
		this.exception = throwable;
	}
	
	/**
	 * A job that immediately returns with <code>value</code>.
	 * @param capability the source of the job
	 * @param input the job input
	 * @param value the value to immediately return with
	 */
	public ImmediateJob(final ILinearCapability<I, V> capability, final I input, final V value) {
		super(capability, input);
		
		if (value == null) {
			throw new IllegalArgumentException("Jobs cannot return null");
		}
		
		this.value = value;
		this.exception = null;
	}
	
	
	@Override
	protected LinearStatus<V> run(final IProgressMonitor monitor) {
		try {
			monitor.beginTask(getName(), 1);
			return value != null ? LinearStatus.makeOk(getCapability(), value) : LinearStatus.<V>makeError(exception);
		} catch (Exception ex) {
			return LinearStatus.<V>makeError(ex);
		} finally {
			monitor.done();
		}
	}
}
