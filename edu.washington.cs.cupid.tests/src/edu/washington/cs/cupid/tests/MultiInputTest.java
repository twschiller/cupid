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

public class MultiInputTest extends AbstractBaseCapability {

	public MultiInputTest() {
		super("Multiple Input Test", "Returns true if inputs are referentially equal", 
			  Lists.newArrayList(PARAM_RESOURCE1, PARAM_RESOURCE2),
			  Lists.newArrayList(OUTPUT),
			  ICapability.Flag.PURE);
	}

	public static final IParameter<IResource> PARAM_RESOURCE1 = new Parameter<IResource>("Resource1", IResource.class);
	public static final IParameter<IResource> PARAM_RESOURCE2 = new Parameter<IResource>("Resource2", IResource.class);
	
	public static final Output<Boolean> OUTPUT = new Output<Boolean>("Same?", TypeToken.of(Boolean.class));

	@Override
	public CapabilityJob<? extends ICapability> getJob(final ICapabilityArguments input) {
	
		return new CapabilityJob<AbstractBaseCapability> (this, input){
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try{
					IResource resource1 = getInputs().getValueArgument(PARAM_RESOURCE1);
					IResource resource2 = getInputs().getValueArgument(PARAM_RESOURCE1);

					OutputBuilder out = new OutputBuilder(MultiInputTest.this);
					out.add(OUTPUT, resource1 == resource2);

					return CapabilityStatus.makeOk(out.getOutputs());
				}finally{
					monitor.done();
				}
			}
		};
		
	}
}
