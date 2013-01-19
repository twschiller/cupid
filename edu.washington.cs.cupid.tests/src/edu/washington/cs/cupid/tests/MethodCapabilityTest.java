package edu.washington.cs.cupid.tests;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class MethodCapabilityTest extends AbstractCapability<IMethod, Boolean> {

	public MethodCapabilityTest(){
		super(
				"Method is foo",
				"edu.washington.cs.cupid.tests.methods.foo",
				"true iff the method is called foo",
				IMethod.class,
				Boolean.class,
				Flag.PURE, Flag.LOCAL);
	}

	@Override
	public CapabilityJob<IMethod, Boolean> getJob(IMethod input) {
		return new CapabilityJob<IMethod, Boolean>(this, input){
			@Override
			protected CapabilityStatus<Boolean> run(IProgressMonitor monitor) {
				monitor.done();
				return CapabilityStatus.makeOk(input.getElementName().equals("foo"));
			}
		};
	}
}
