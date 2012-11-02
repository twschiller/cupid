package edu.washington.cs.cupid.jdt.compiler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class CompilerMessageCapability extends AbstractCapability<ICompilationUnit, Integer> {

	public CompilerMessageCapability(){
		super("Compiler Messages", 
			  "edu.washington.cs.cupid.jdt.messages", 
			  "Number of compiler messages (e.g., warnings and errors)",
			  ICompilationUnit.class,
			  Integer.class,
			  Flag.PURE, Flag.LOCAL);
	}
	
	@Override
	public CapabilityJob<ICompilationUnit, Integer> getJob(ICompilationUnit input) {
		return new CapabilityJob<ICompilationUnit, Integer>(this, input){
			@Override
			protected CapabilityStatus<Integer> run(IProgressMonitor monitor) {
				try{
					CompilationUnit unit = ParseUtil.parse(input, monitor);	
					return CapabilityStatus.makeOk(unit.getMessages().length);
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}
}
