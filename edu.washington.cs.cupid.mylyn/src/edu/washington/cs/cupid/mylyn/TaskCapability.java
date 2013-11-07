package edu.washington.cs.cupid.mylyn;

import org.eclipse.mylyn.tasks.core.ITask;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.ImmediateJob;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;

public class TaskCapability extends LinearCapability<ITask, ITask> {

	public TaskCapability(){
		super("Task (Identity)", "Returns the tasks itself, or the corresponding task",
			  ITask.class, ITask.class, 
			  ICapability.Flag.PURE);
	}

	@Override
	public LinearJob<ITask, ITask> getJob(ITask input) {
		return new ImmediateJob<ITask, ITask>(this, input, input);
	}
}
