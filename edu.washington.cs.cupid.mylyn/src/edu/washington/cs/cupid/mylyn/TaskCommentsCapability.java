package edu.washington.cs.cupid.mylyn;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.TaskComment;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public final class TaskCommentsCapability extends LinearCapability<ITask, List<ITaskComment>> {

	public TaskCommentsCapability(){
		super("Mylyn Task Comments", "Get the comments for a Mylyn task",
			TypeToken.of(ITask.class), new TypeToken<List<ITaskComment>>(){},
			ICapability.Flag.PURE);
	}

	@Override
	public LinearJob<ITask, List<ITaskComment>> getJob(ITask input) {
		return new LinearJob<ITask, List<ITaskComment>>(this, input) {
			@Override
			protected LinearStatus<List<ITaskComment>> run(final IProgressMonitor monitor) {
				try {
					ITask task = getInput();
					
					TaskData data = TasksUiPlugin.getTaskDataManager().getTaskData(getInput());
					List<TaskAttribute> commentAttributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
					TaskRepository repository =TasksUiPlugin.getRepositoryManager().getRepository(data.getRepositoryUrl());
					
					List<ITaskComment> result = Lists.newArrayList();
					
					for (TaskAttribute a : commentAttributes){
						result.add(new TaskComment(repository, task, a));
					}
					
					return new LinearStatus<List<ITaskComment>>(TaskCommentsCapability.this, result);
					
				} catch (Exception e) {
					return new LinearStatus<List<ITaskComment>>(e);
				} finally {
					monitor.done();
				}
			}
		};
	} 
}
