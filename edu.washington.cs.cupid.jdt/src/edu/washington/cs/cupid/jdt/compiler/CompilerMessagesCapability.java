package edu.washington.cs.cupid.jdt.compiler;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class CompilerMessagesCapability extends AbstractCapability<ICompilationUnit, List<Message>> {
	
	public final static TypeToken<List<Message>> COMPILER_MESSAGES = new TypeToken<List<Message>>(){
		private static final long serialVersionUID = 1L;
	};
	
	public CompilerMessagesCapability(){
		super("Compiler Messages", 
			  "edu.washington.cs.cupid.jdt.messages", 
			  "Compiler messages (e.g., warnings and errors)",
			  TypeToken.of(ICompilationUnit.class),
			  COMPILER_MESSAGES,
			  Flag.PURE);
	}
	
	@Override
	public CapabilityJob<ICompilationUnit, List<Message>> getJob(ICompilationUnit input) {
		return new CapabilityJob<ICompilationUnit, List<Message>>(this, input){
			@Override
			protected CapabilityStatus<List<Message>> run(IProgressMonitor monitor) {
				try{
					CompilationUnit unit = ParseUtil.parse(input, monitor);	
					return CapabilityStatus.makeOk(Arrays.asList(unit.getMessages()));
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}
}
