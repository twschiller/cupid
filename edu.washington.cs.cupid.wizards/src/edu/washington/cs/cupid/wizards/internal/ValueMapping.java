package edu.washington.cs.cupid.wizards.internal;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.dynamic.DynamicPipeline;

public class ValueMapping<I,V> extends DynamicPipeline<I,Map<I,Set<V>>> {

	private static final long serialVersionUID = 1L;
	private final static String BASE_ID = "edu.washington.cs.cupid.wizards.internal.mapping.value";
	
	private TypeToken<I> inputType;
	private TypeToken<V> valueType;
	
	private String valueGenerator;
	private String keyLink;
	private String valueLink;
	
	public ValueMapping(
			String name,
			String description,
			TypeToken<I> inputType, String keyLink,
			String valueGenerator, TypeToken<V> valueType, String valueLink){
		
		super(name, description, Sets.<Serializable>newHashSet(valueGenerator));
		this.valueGenerator = valueGenerator;
		this.keyLink = keyLink;
		this.valueType = valueType;
		this.valueLink = valueLink;
	}
	
	@Override
	public String getUniqueId() {
		return BASE_ID + ".[" + inputType.getRawType().getName() + "].[" + valueGenerator + "]"; 
	}

	@Override
	public TypeToken<I> getParameterType() {
		return inputType;
	}

	@SuppressWarnings("serial")
	@Override
	public TypeToken<Map<I,Set<V>>> getReturnType() {
		 return new TypeToken<Map<I,Set<V>>>(getClass()){}
		 	.where(new TypeParameter<I>(){}, inputType)
		 	.where(new TypeParameter<V>(){}, valueType);
	}

	@Override
	public Set<String> getDynamicDependencies() {
		return Sets.newHashSet(valueGenerator);
	}

	private Object link(Object value, String valueLink) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		return valueLink == null 
				? value
				: value.getClass().getMethod(valueLink).invoke(value);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CapabilityJob getJob(Object input) {
		return new CapabilityJob(this, input){
			@Override
			protected CapabilityStatus<Map<I, Set<V>>> run(IProgressMonitor monitor) {
				try{
					Object key = link(input, keyLink);
					
					CapabilityJob<?,?> subtask = current().get(valueGenerator).getJob(null);
					
					if (subtask == null){
						throw new RuntimeException("Value generator produced null job");
					}
					
					subtask.schedule();
					try {
						subtask.join();
					} catch (InterruptedException e) {
						monitor.done();
						return CapabilityStatus.makeError(e);
					}

					CapabilityStatus<Collection<V>> status = ((CapabilityStatus<Collection<V>>)subtask.getResult());
					
					if (status.getCode() == Status.OK){

						Set<V> collection = Sets.newHashSet();
						
						for (V v : status.value()){
							if (key.equals(link(v, valueLink))){
								collection.add(v);
							}
						}
						
						Map<I, Set<V>> result = Maps.newHashMap();
						result.put((I) input, collection);
						
						return CapabilityStatus.makeOk(result);
					}else{
						monitor.done();
						return CapabilityStatus.makeError(status.getException());
					}
				
					
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}

}
