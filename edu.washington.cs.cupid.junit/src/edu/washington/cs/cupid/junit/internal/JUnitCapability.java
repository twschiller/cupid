package edu.washington.cs.cupid.junit.internal;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.junit.JUnitJob;

@SuppressWarnings({ "restriction" })
public class JUnitCapability extends AbstractCapability<IJavaProject, TestRunSession>{

	private final String configuration;
	
	public JUnitCapability(String configuration){
		super(  "JUnit",
				"edu.washington.cs.cupid.junit." + configuration,
				"JUnit test failures",	
				IJavaProject.class,
				TestRunSession.class,
				Flag.PURE);
		
		this.configuration = configuration;
	}
	
	@Override
	public CapabilityJob<IJavaProject, TestRunSession> getJob(IJavaProject input) {
		return new JUnitJob(this, configuration, input);
	}
}
