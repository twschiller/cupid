package edu.washington.cs.cupid.mylyn;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * @author Todd Schiller
 */
public class TaskContextCapability extends LinearCapability<AbstractTask, List<IResource>>{

	public TaskContextCapability() {
		super(
			"Task Context", "Returns the Resources in the Context for the Task",
			TypeToken.of(AbstractTask.class), new TypeToken<List<IResource>>(){},
			ICapability.Flag.PURE);
	}

	@Override
	public LinearJob<AbstractTask, List<IResource>> getJob(final AbstractTask input) {
		return new LinearJob<AbstractTask, List<IResource>>(this, input) {
			@Override
			protected LinearStatus<List<IResource>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
				
					AbstractTask task = getInput();
					
					IInteractionContext context = null;
					
					//http://git.eclipse.org/c/mylyn/org.eclipse.mylyn.context.git/tree/org.eclipse.mylyn.context.tasks.ui/src/org/eclipse/mylyn/internal/context/tasks/ui/editors/ContextEditorFormPage.java
					if (isActiveTask(task)){
						context = ContextCorePlugin.getContextManager().getActiveContext();
					}else{
						context = ContextCorePlugin.getContextStore().loadContext(task.getHandleIdentifier());
					}
						
					List<IResource> result = Lists.newArrayList();
					
					for (IInteractionElement x : context.getAllElements()){
						if (x.getContentType().equals("resource")){
							IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(x.getHandleIdentifier());
							if (r != null){
								result.add(r);
							}
						}
					}
					
					return LinearStatus.makeOk(getCapability(), result);
				} catch (Exception ex) {
					return LinearStatus.<List<IResource>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}


	private boolean isActiveTask(final AbstractTask task) {
		return task.equals(TasksUi.getTaskActivityManager().getActiveTask());
	}
}
