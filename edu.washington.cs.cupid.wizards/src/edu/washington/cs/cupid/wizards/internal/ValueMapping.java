package edu.washington.cs.cupid.wizards.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ISerializableCapability;

public class ValueMapping<I,V> extends AbstractMapping<I,I,V> {

	private static final long serialVersionUID = 1L;
	
	private final static String BASE_ID = "edu.washington.cs.cupid.wizards.internal.mapping.value";
		
	private ISerializableCapability<?,Collection<V>> valueGenerator;
	private String keyLink;
	private String valueLink;
	
	public ValueMapping(
			String name,
			String description,
			TypeToken<I> inputType, String keyLink,
			ISerializableCapability<?,Collection<V>> valueGenerator, TypeToken<V> valueType, String valueLink){
	
		super(name, description, inputType, inputType, valueType);
		this.valueGenerator = valueGenerator;
		this.keyLink = keyLink;
		this.valueLink = valueLink;
	}
	
	@Override
	public String getUniqueId() {
		return BASE_ID + ".[" + inputType.getRawType().getName() + "].[" + valueGenerator.getUniqueId() + "]"; 
	}

	@Override
	public Set<String> getDynamicDependencies() {
		return valueGenerator.getDynamicDependencies();
	}

	private Object link(Object value, String valueLink) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		return valueLink == null 
				? value
				: value.getClass().getMethod(valueLink).invoke(value);
	}
	

	@Override
	public CapabilityJob<I, Map<I,Set<V>>> getJob(I input) {
		return new CapabilityJob<I, Map<I,Set<V>>>(this, input){
			@Override
			protected CapabilityStatus<Map<I, Set<V>>> run(IProgressMonitor monitor) {
				try{
					Object key = link(input, keyLink);

					CapabilityJob<?,Collection<V>> subtask = valueGenerator.getJob(null);
					monitor.subTask("Generating Values");
					Collection<V> values = runSubtask(subtask, monitor);


					Set<V> collection = Sets.newHashSet();

					for (V v : values){
						if (key.equals(link(v, valueLink))){
							collection.add(v);
						}
					}

					Map<I, Set<V>> result = Maps.newHashMap();
					result.put((I) input, collection);

					return CapabilityStatus.makeOk(result);

				}catch(Throwable ex){
					monitor.done();
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}

	@Override
	public boolean isPure() {
		return valueGenerator.isPure();
	}

	@Override
	public boolean isLocal() {
		return valueGenerator.isLocal();
	}

	@Override
	public boolean isTransient() {
		return valueGenerator.isTransient();
	}

}
