package edu.washington.cs.cupid.capability;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public abstract class AbstractBaseCapability extends AbstractCapability {

	private EnumSet<Flag> flags;
	private LinkedHashSet<? extends IParameter<?>> parameters;
	private LinkedHashSet<? extends IOutput<?>> outputs;
	
	public AbstractBaseCapability(String name, String uniqueId, String description,
			List<? extends IParameter<?>> parameters, List<? extends IOutput<?>> outputs,
			Flag...flags) {
		super(name, uniqueId, description);

		this.flags = EnumSet.noneOf(Flag.class);
		this.flags.addAll(Arrays.asList(flags));
		
		this.parameters = Sets.newLinkedHashSet(parameters);
		this.outputs = Sets.newLinkedHashSet(outputs);
	}

	
	@Override
	public Set<? extends IParameter<?>> getParameters() {
		return Collections.unmodifiableSet(this.parameters);
	}

	@Override
	public Set<? extends IOutput<?>> getOutputs() {
		return Collections.unmodifiableSet(this.outputs);
	}

	@Override
	public EnumSet<Flag> getFlags() {
		return flags;
	}

}
