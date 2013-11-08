package edu.washington.cs.cupid.mylyn;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class TasksForResource extends LinearCapability<IResource, List<AbstractTask>> {

	@SuppressWarnings("serial")
	public TasksForResource() {
		super("Mylyn Tasks for Resource", "Returns the tasks that include the resource in their context",
			  TypeToken.of(IResource.class), new TypeToken<List<AbstractTask>>(){}, ICapability.Flag.PURE);
	}

	@Override
	public LinearJob<IResource, List<AbstractTask>> getJob(IResource input) {
		return new LinearJob<IResource, List<AbstractTask>>(this, input){

			@Override
			protected LinearStatus<List<AbstractTask>> run(IProgressMonitor monitor) {
				try {
					IResource input = getInput();	
					
					TaskList taskList = TasksUiPlugin.getTaskList();
					Collection<AbstractTask> tasks = taskList.getAllTasks();
					
					monitor.beginTask(getName(), tasks.size());
					
					List<AbstractTask> result = Lists.newArrayList();
					
					for (AbstractTask task : tasks){
						IInteractionContext context = null;
						
						//http://git.eclipse.org/c/mylyn/org.eclipse.mylyn.context.git/tree/org.eclipse.mylyn.context.tasks.ui/src/org/eclipse/mylyn/internal/context/tasks/ui/editors/ContextEditorFormPage.java
						if (isActiveTask(task)){
							context = ContextCorePlugin.getContextManager().getActiveContext();
						}else{
							context = ContextCorePlugin.getContextStore().loadContext(task.getHandleIdentifier());
						}
						
						boolean inContext = false;
						for (IInteractionElement x : context.getAllElements()){
							if (x.getContentType().equals("resource")){
								IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(x.getHandleIdentifier());
								if (r != null){
									if (r.equals(getInput()) ||
										input instanceof IContainer && ((IContainer) input).contains(r)){
										
										inContext = true;
										break;
									}
								}
							}
						}
						
						if (inContext){
							result.add(task);
						}
			
						monitor.worked(1);
					}

					return LinearStatus.makeOk(getCapability(), result);
				} catch (Exception ex) {
					return LinearStatus.<List<AbstractTask>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
	
	private boolean isActiveTask(final ITask task) {
		return task.equals(TasksUi.getTaskActivityManager().getActiveTask());
	}
}
