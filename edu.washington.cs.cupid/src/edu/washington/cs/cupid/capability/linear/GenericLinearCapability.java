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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.CapabilityOutputs;
import edu.washington.cs.cupid.capability.Parameter;

public abstract class GenericLinearCapability<I, V> extends AbstractCapability implements ILinearCapability<I, V> {

	private IParameter<I> input;
	private IOutput<V> output;
	private EnumSet<Flag> flags;
	
	public GenericLinearCapability(String name, String uniqueId,
			String description, 
			Flag... flags) {
		super(name, uniqueId, description);
		
		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
	}

	@Override
	public final Set<IParameter<?>> getParameters() {
		return Collections.<IParameter<?>>singleton(getParameter());
	}

	@Override
	public final Set<IOutput<?>> getOutputs() {
		return Collections.<IOutput<?>>singleton(getOutput());
	}

	@Override
	public final IParameter<I> getParameter() {
		if (input == null){
			input = new Parameter<I>(null, getInputType());
		}
		return input;
	}

	@Override
	public final IOutput<V> getOutput() {
		if (output == null){
			output = new CapabilityOutputs<V>(null, getOutputType());
		}
		return output;
	}

	@Override
	public final LinearJob<I, V> getJob(final ICapabilityArguments input) {
		return getJob(input.getValueArgument(getParameter()));
	}
	
	@Override
	public EnumSet<Flag> getFlags() {
		return flags;
	}

	public abstract TypeToken<I> getInputType();
	
	public abstract TypeToken<V> getOutputType();
}