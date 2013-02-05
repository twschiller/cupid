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
package edu.washington.cs.cupid.capability;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.collect.Sets;

import edu.washington.cs.cupid.internal.CupidActivator;

/**
 * <p>An Eclipse {@link Job} for executing all or part of a Cupid capability. Produces
 * an output for the input, similar to a future (sometimes called a promise). The overridden 
 * {@link Job#run()} method must set the output value for successful completions.</p>
 * 
 * <p>The starts out in three job families: Cupid's family, it's input's family, and
 * it's capability's family. More familiy associations can be made using the {@link #addFamily(Object)}
 * method.</p>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> input type
 * @param <V> output type
 * @see {@link Job}
 */
public abstract class CapabilityJob extends Job {
	
	/**
	 * The input to the capability.
	 */
	// TODO make private and expose getter
	private final ICapabilityInput input;
	
	private final ICapability capability;
	
	private final Set<Object> families;
	
	/**
	 * A capability job produced by <code>capability</code> operating on input <code>input</code>.
	 * @param capability the source capability
	 * @param input the input
	 */
	public CapabilityJob(final ICapability capability, final ICapabilityInput input) {
		super(capability.getUniqueId());
		this.input = input;
		this.capability = capability;
		this.families = Sets.newHashSet((Object) input, CupidActivator.getDefault(), capability);
	}
	
	/**
	 * @return the associated capability
	 */
	public final ICapabilityInput getInputs() {
		return input;
	}
	
	/**
	 * @return the associated capability
	 */
	public final ICapability getCapability() {
		return capability;
	}
	
	/**
	 * Add this job to <code>family</code>. Does nothing if the job is already a member
	 * of the <code>family</code>.
	 * @param family the family
	 * @see {@link CapabilityJob#belongsTo(Object)}
	 */
	public final void addFamily(final Object family) {
		families.add(family);
	}
	
	@Override
	public final boolean belongsTo(final Object family) {
		return families.contains(family);
	}
	
	@Override 
	protected abstract CapabilityStatus run(IProgressMonitor monitor);
	
}
