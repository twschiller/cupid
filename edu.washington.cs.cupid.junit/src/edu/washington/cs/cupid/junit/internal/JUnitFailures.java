package edu.washington.cs.cupid.junit.internal;

import java.util.Set;

import org.eclipse.jdt.internal.junit.model.TestElement;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.jobs.ImmediateJob;
import edu.washington.cs.cupid.junit.Types;

@SuppressWarnings("restriction")
public class JUnitFailures extends AbstractCapability<TestRunSession, Set<TestElement>>{

	public JUnitFailures(){
		super(  "JUnit Failed Tests",
				"edu.washington.cs.cupid.junit.internal.failed",
				"Extract failed tests",
				TypeToken.of(TestRunSession.class),
				Types.TEST_ELEMENTS,
				Flag.PURE, Flag.LOCAL);
	}
	
	@Override
	public CapabilityJob<TestRunSession, Set<TestElement>> getJob(TestRunSession input) {
		return new ImmediateJob<TestRunSession, Set<TestElement>>(this, input, Sets.newHashSet(input.getAllFailedTestElements()));
	}

}
