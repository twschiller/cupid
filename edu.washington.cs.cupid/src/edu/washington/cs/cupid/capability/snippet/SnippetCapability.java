package edu.washington.cs.cupid.capability.snippet;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.linear.GenericLinearSerializableCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class SnippetCapability<I,V> extends GenericLinearSerializableCapability<I,V> {

	private static final long serialVersionUID = 1L;
	private TypeToken<I> inputType;
	private TypeToken<V> outputType;
	private String snippet;
	
	public SnippetCapability(
			String name, String uniqueId, String description, 
			TypeToken<I> inputType, TypeToken<V> outputType, String snippet) {
		
		super(name, uniqueId, description, Flag.PURE);
		this.inputType = inputType;
		this.outputType = outputType;
		this.snippet = snippet;
	}
	
	@Override
	public LinearJob<I, V> getJob(final I input) {
		return new LinearJob<I,V>(this, input){
			@Override
			protected CapabilityStatus run(IProgressMonitor monitor) {
				try{
					monitor.beginTask("Execute Snippet", 1);
					V val = SnippetEvalManager.getInstance().run(SnippetCapability.this, input);
					return LinearStatus.makeOk(getCapability(), val);	
				}catch (InvocationTargetException ex){
					return LinearStatus.<V>makeError(ex.getTargetException());
				}catch(Exception ex){
					return LinearStatus.<V>makeError(ex);
				}finally{
					monitor.done();
				}
			}
		};
	}

	public String getSnippet(){
		return snippet;
	}
	
	@Override
	public TypeToken<I> getInputType() {
		return inputType;
	}

	@Override
	public TypeToken<V> getOutputType() {
		return outputType;
	}
}
