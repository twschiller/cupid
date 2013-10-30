package edu.washington.cs.cupid.mylyn;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractBaseCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.OptionalParameter;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.Parameter;

public class TaskFilter extends AbstractBaseCapability {
	
	public static final IParameter<Collection<AbstractTask>> TASKS = new Parameter<Collection<AbstractTask>>("Tasks", new TypeToken<Collection<AbstractTask>>(){});
	public static final IParameter<Boolean> FILTER_COMPLETE = new OptionalParameter<Boolean>("Exclude Completed Tasks", Boolean.class, false);
	public static final IParameter<Boolean> FILTER_INCOMPLETE = new OptionalParameter<Boolean>("Exclude Incomplete Tasks", Boolean.class, false);
	public static final Output<Collection<AbstractTask>> FILTERED = new Output<Collection<AbstractTask>>("Filtered", new TypeToken<Collection<AbstractTask>>(){});
		
	public TaskFilter() {
		super("Task Filter", "Filters tasks according to the selected options", 
			  Lists.newArrayList(TASKS, FILTER_COMPLETE, FILTER_INCOMPLETE), Lists.newArrayList(FILTERED),
			  ICapability.Flag.PURE);
	}
	
	@Override
	public CapabilityJob<? extends ICapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<TaskFilter>(this, input) {
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try {
					Collection<AbstractTask> tasks = input.getValueArgument(TASKS);
					boolean filterComplete = input.getValueArgument(FILTER_COMPLETE);
					boolean filterIncomplete = input.getValueArgument(FILTER_INCOMPLETE);
					
					monitor.beginTask(getName(), tasks.size());
					
					List<AbstractTask> result = Lists.newArrayList();
					
					for (AbstractTask task : tasks){
						monitor.worked(1);
						
						if (filterComplete && task.isCompleted()){
							continue;
						} else if (filterIncomplete && !task.isCompleted()){
							continue;
						} else{
							result.add(task);
						}
					}

					return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(FILTERED, result));

				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
