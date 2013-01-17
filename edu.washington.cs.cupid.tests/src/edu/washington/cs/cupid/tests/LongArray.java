package edu.washington.cs.cupid.tests;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class LongArray extends AbstractCapability<IResource, Object[]>  {

	public LongArray(){
		super(
				"Long Array",
				"edu.washington.cs.cupid.tests.longarray",
				"Returns a long array",
				TypeToken.of(IResource.class), new TypeToken<Object[]>(){},
				Flag.PURE, Flag.LOCAL);
	}
	
	@Override
	public CapabilityJob<IResource, Object[]> getJob(IResource input) {
		
		return new CapabilityJob<IResource,Object[]>(this, input){

			@Override
			protected CapabilityStatus<Object[]> run(IProgressMonitor monitor) {
				int size = 1500;
				monitor.beginTask("Create long array", size);
				
				List<Integer> result = Lists.newArrayList();
				for (int i = 0; i < size; i++){
					monitor.worked(1);
					result.add(i);
				}
				monitor.done();
				return CapabilityStatus.makeOk(result.toArray());
			}
			
		};
	}

}
