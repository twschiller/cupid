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

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.AbstractSerializableCapability;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.Parameter;

public abstract class GenericLinearSerializableCapability<I, V> extends AbstractSerializableCapability implements ILinearCapability<I, V> {

	private static final long serialVersionUID = 2L;

	private IParameter<I> input;
	private IOutput<V> output;
	private EnumSet<Flag> flags;
	
	public GenericLinearSerializableCapability(String name, String description, Flag... flags) {
		super(name, description);
		
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
			input = new Parameter<I>(TypeManager.simpleTypeName(getInputType()), getInputType());
		}
		return input;
	}

	@Override
	public final IOutput<V> getOutput() {
		if (output == null){
			output = new Output<V>(TypeManager.simpleTypeName(getOutputType()), getOutputType());
		}
		return output;
	}

	@Override
	public final LinearJob<I, V> getJob(final ICapabilityArguments input) {
		if (CapabilityUtil.isGenerator(this)){
			return getJob((I) null);
		} else {
			return getJob(input.getValueArgument(getParameter()));
		}
	}
	
	@Override
	public EnumSet<Flag> getFlags() {
		return flags;
	}

	public abstract TypeToken<I> getInputType();
	
	public abstract TypeToken<V> getOutputType();
}
