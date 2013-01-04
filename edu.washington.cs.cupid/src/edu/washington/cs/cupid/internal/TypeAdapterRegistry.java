package edu.washington.cs.cupid.internal;

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.types.ITypeAdapter;
import edu.washington.cs.cupid.types.ITypeAdapterRegistry;

public class TypeAdapterRegistry implements ITypeAdapterRegistry {

	private Multimap<TypeToken<?>, ITypeAdapter<?,?>> registry = HashMultimap.create();
	
	@Override
	public void registerAdapter(ITypeAdapter<?,?> adapter){
		TypeToken<?> inputType = adapter.getInputType();
		registry.put(inputType, adapter);
	}
	
	@Override
	public ITypeAdapter<?, ?>[] getTypeAdapters(TypeToken<?> inputType) {
		Set<ITypeAdapter<?,?>> result = Sets.newHashSet();
		
		for (TypeToken<?> adapterInput : registry.keys()){
			if (TypeManager.isJavaCompatible(adapterInput, inputType)){
				result.addAll(registry.get(adapterInput));
			}
		}
		
		return result.toArray(new ITypeAdapter[]{});
	}

	@Override
	public ITypeAdapter<?,?> getTypeAdapter(TypeToken<?> inputType, TypeToken<?> outputType) {
		ITypeAdapter<?,?> result = null;
		for (ITypeAdapter<?,?> adapter : getTypeAdapters(inputType)){
			if (TypeManager.isJavaCompatible(outputType, adapter.getOutputType())){
				result = adapter;
			}
		}
		return result;
	}

}
