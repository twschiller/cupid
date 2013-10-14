package edu.washington.cs.cupid.wizards.internal;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.GenericLinearSerializableCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class LiftedCapability<I,V> extends GenericLinearSerializableCapability<List<I>, List<V>>{
	private static final long serialVersionUID = 1L;

	private ICapability capability;
	private String capabilityName;
	
	private TypeToken<I> inputType;
	private TypeToken<V> outputType;
	
	public LiftedCapability(ICapability capability) {
		super("Lifted: " + capability.getName(), 
			  "Perform " + capability.getName() + " on a list",
			  capability.getFlags().toArray(new ICapability.Flag[]{}));
		
		this.inputType = (TypeToken<I>) CapabilityUtil.unaryParameter(capability).getType();
		this.outputType = (TypeToken<V>) CapabilityUtil.singleOutput(capability).getType();
		
		if (CapabilityUtil.isSerializable(capability)){
			this.capability = capability;
			this.capabilityName = null;
		}else{
			this.capability = null;
			this.capabilityName = capability.getName();
		}	
	}

	@Override
	public LinearJob<List<I>, List<V>> getJob(List<I> input) {
		return new LinearJob<List<I>, List<V>>(this, input){
			@Override
			protected LinearStatus<List<V>> run(final IProgressMonitor monitor) {
				try{
					ICapability c = capability != null ? capability : CupidPlatform.getCapabilityRegistry().findCapability(capabilityName);
					
					List<I> input = getInput();
					
					monitor.beginTask(getName(), input.size());
					
					List<V> result = Lists.newArrayList();
					
					for (I elt : input){
						if (monitor.isCanceled()){
							throw new InterruptedException("Capability Job was cancelled");
						}
						
						CapabilityJob<?> subtask = c.getJob(CapabilityUtil.packUnaryInput(c, elt));
						subtask.schedule();
						subtask.join();
						
						CapabilityStatus status = (CapabilityStatus) subtask.getResult();
						
						if (!status.isOK() || status.getException() != null){
							throw status.getException();
						}
						
						result.add((V) CapabilityUtil.singleOutputValue(c, status));
						
						monitor.worked(1);
					}
					
					return LinearStatus.makeOk(getCapability(), result);

				}catch(Throwable t){
					return LinearStatus.<List<V>>makeError(t);
				}finally{
					monitor.done();
				}
			}
		};
	}

	@Override
	public TypeToken<List<I>> getInputType() {
		return new TypeToken<List<I>>(getClass()){}
	 		.where(new TypeParameter<I>(){}, inputType );
	}

	@Override
	public TypeToken<List<V>> getOutputType() {
		return new TypeToken<List<V>>(getClass()){}
 			.where(new TypeParameter<V>(){}, outputType);
	}
}
