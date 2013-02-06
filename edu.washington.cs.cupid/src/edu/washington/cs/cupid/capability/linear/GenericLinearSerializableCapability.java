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

import edu.washington.cs.cupid.capability.AbstractSerializableCapability;
import edu.washington.cs.cupid.capability.ICapabilityInput;
import edu.washington.cs.cupid.capability.OutputImpl;
import edu.washington.cs.cupid.capability.ParameterImpl;

public abstract class GenericLinearSerializableCapability<I, V> extends AbstractSerializableCapability implements ILinearCapability<I, V> {

	private static final long serialVersionUID = 1L;

	private Parameter<I> input;
	private Output<V> output;
	private EnumSet<Flag> flags;
	
	public GenericLinearSerializableCapability(String name, String uniqueId,
			String description, 
			Flag... flags) {
		super(name, uniqueId, description);
		
		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
	}

	@Override
	public final Set<Parameter<?>> getParameters() {
		return Collections.<Parameter<?>>singleton(getParameter());
	}

	@Override
	public final Set<Output<?>> getOutputs() {
		return Collections.<Output<?>>singleton(getOutput());
	}

	@Override
	public final Parameter<I> getParameter() {
		if (input == null){
			input = new ParameterImpl<I>(null, getInputType());
		}
		return input;
	}

	@Override
	public final Output<V> getOutput() {
		if (output == null){
			output = new OutputImpl<V>(null, getOutputType());
		}
		return output;
	}

	@Override
	public final LinearJob<I, V> getJob(final ICapabilityInput input) {
		return getJob(input.getArgument(getParameter()));
	}
	
	@Override
	public EnumSet<Flag> getFlags() {
		return flags;
	}

	public abstract TypeToken<I> getInputType();
	
	public abstract TypeToken<V> getOutputType();
}
