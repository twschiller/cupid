package edu.washington.cs.cupid.standard;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;

public class MostFrequent<V> extends GenericAbstractCapability<List<V>, V>{
	
	// TODO does type erasure break this?
	// TODO make efficient
	
	public MostFrequent(){
		super(
				"Most Frequent", 
				"edu.washington.cs.cupid.standard.frequent",
				"Get the most frequent element in a collection",
				Flag.PURE);
	}
	
	@Override
	public CapabilityJob<List<V>, V> getJob(List<V> input) {
		return new CapabilityJob<List<V>,V>(this, input){
			@Override
			protected CapabilityStatus<V> run(IProgressMonitor monitor) {
				Multiset<V> set = HashMultiset.create();
				set.addAll(input);
				for (V val : Multisets.copyHighestCountFirst(set)){
					return CapabilityStatus.makeOk(val);
				}
				return CapabilityStatus.makeError(new IllegalArgumentException("Cannot get most frequent element of empty collection"));
				
			}
		};
	}

	@Override
	public TypeToken<List<V>> getParameterType() {
		return new TypeToken<List<V>>(getClass()){
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public TypeToken<V> getReturnType() {
		return new TypeToken<V>(getClass()){
			private static final long serialVersionUID = 1L;
		};
	}

}
