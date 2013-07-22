package edu.washington.cs.cupid.tests;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractBaseCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.OutputBuilder;
import edu.washington.cs.cupid.capability.Parameter;

public class MultipleOutputTest extends AbstractBaseCapability {

	public MultipleOutputTest() {
		super("Multiple Output Test", "Capability with multiple outputs", 
			  Lists.newArrayList(PARAM_RESOURCE),
			  Lists.newArrayList(OUT_STRING1, OUT_STRING2, OUT_INTEGER),
			  ICapability.Flag.PURE);
	}

	public static final IParameter<IResource> PARAM_RESOURCE = new Parameter<IResource>("Resource", IResource.class);
	
	public static final Output<String> OUT_STRING1 = new Output<String>("String1", TypeToken.of(String.class));
	public static final Output<String> OUT_STRING2 = new Output<String>("String2", TypeToken.of(String.class));
	public static final Output<Integer> OUT_INTEGER = new Output<Integer>("Integer", TypeToken.of(Integer.class));

	@Override
	public CapabilityJob<MultipleOutputTest> getJob(ICapabilityArguments input) {
	
		return new CapabilityJob<MultipleOutputTest> (this, input){
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				
				OutputBuilder out = new OutputBuilder(MultipleOutputTest.this);
				out.add(OUT_STRING1, "This is the first string");
				out.add(OUT_STRING2, "This is the second string");
				out.add(OUT_INTEGER, 3);
				
				return CapabilityStatus.makeOk(out.getOutputs());
			}
		};
		
	}
}
