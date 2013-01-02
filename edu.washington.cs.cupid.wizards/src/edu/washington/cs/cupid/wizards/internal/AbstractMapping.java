package edu.washington.cs.cupid.wizards.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ISerializableCapability;

public abstract class AbstractMapping<I,K,V> implements ISerializableCapability<I,Map<K,Set<V>>> {

	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	
	protected final TypeToken<K> keyType;
	protected final TypeToken<I> inputType;
	protected final TypeToken<V> valueType;
	
	public AbstractMapping(String name, String description, 
			TypeToken<I> inputType, TypeToken<K> keyType, TypeToken<V> valueType){
		this.name = name;
		this.description = description;
		this.inputType = inputType;
		this.keyType = keyType;
		this.valueType = valueType;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public final TypeToken<I> getParameterType() {
		return inputType;
	}

	@SuppressWarnings("serial")
	@Override
	public final TypeToken<Map<K,Set<V>>> getReturnType() {
		 return new TypeToken<Map<K,Set<V>>>(getClass()){}
		 	.where(new TypeParameter<K>(){}, keyType)
		 	.where(new TypeParameter<V>(){}, valueType);
	}
	
	protected <R> R runSubtask(CapabilityJob<?, R> subtask, IProgressMonitor monitor) throws Throwable{
		
		subtask.schedule();
		subtask.join();
		
		CapabilityStatus<R> status = ((CapabilityStatus<R>)subtask.getResult());
		
		if (status.getCode() == Status.OK){
			return status.value();
		}else{
			throw status.getException();
		}
	}
	
}