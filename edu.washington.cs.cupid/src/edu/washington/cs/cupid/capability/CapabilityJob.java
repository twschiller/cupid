package edu.washington.cs.cupid.capability;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.internal.CupidActivator;

/**
 * <p>An Eclipse {@link Job} for executing all or part of a Cupid capability. Produces
 * an output for the input, similar to a future (sometimes called a promise). The overridden 
 * {@link Job#run()} method must set the output value for successful completions.</p>
 * 
 * <p>The starts out in three job families: Cupid's family, it's input's family, and
 * it's capability's family. More familiy associations can be made using the {@link #addFamily(Object)}
 * method.</p>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <I> input type
 * @param <V> output type
 * @see {@link Job}
 */
public abstract class CapabilityJob<I, V> extends Job{
	protected final I input;
	private final ICapability<I,V> capability;
	private final Set<Object> families;
	
	public CapabilityJob(ICapability<I,V> capability, I input) {
		super(capability.getUniqueId());
		this.input = input;
		this.capability = capability;
		this.families = Sets.newHashSet((Object) input, CupidActivator.getDefault(), capability);
	}
	
	/**
	 * @return the associated capability
	 */
	public final I getInput() {
		return input;
	}
	
	/**
	 * @return the associated capability
	 */
	public final ICapability<I, V> getCapability() {
		return capability;
	}
	
	/**
	 * Add this job to <code>family</code>. Does nothing if the job is already a member
	 * of the <code>family</code>.
	 * @param family the family
	 * @see {@link CapabilityJob#belongsTo(Object)}
	 */
	public final void addFamily(Object family){
		families.add(family);
	}
	
	@Override
	public final boolean belongsTo(Object family){
		return families.contains(family);
	}
	
	@Override 
	protected abstract CapabilityStatus<V> run(IProgressMonitor monitor);
	
}
