package edu.washington.cs.cupid.mylyn;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;

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

/**
 * @author Todd Schiller
 */
public class TaskContextCapability extends AbstractBaseCapability{

	public static final IParameter<ITask> TASK = new Parameter<ITask>("Task", new TypeToken<ITask>(){});
	public static final IParameter<Boolean> INCLUDE_FOLDERS = new OptionalParameter<Boolean>("Include Folders", Boolean.class, false);
	public static final IParameter<Boolean> INCLUDE_FILES = new OptionalParameter<Boolean>("Include Files", Boolean.class, true);
	public static final Output<List<IResource>> CONTEXT = new Output<List<IResource>>("Context", new TypeToken<List<IResource>>(){});
		
	public TaskContextCapability() {
		super(
			"Mylyn Task Context", "Returns the Resources in the Context for the Task",
			Lists.newArrayList(TASK, INCLUDE_FOLDERS, INCLUDE_FILES),
			Lists.newArrayList(CONTEXT),
			ICapability.Flag.PURE);
	}

	@Override
	public CapabilityJob<TaskContextCapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<TaskContextCapability>(this, input) {
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
				
					ITask task = input.getValueArgument(TASK);
					boolean includeFolders = input.getValueArgument(INCLUDE_FOLDERS);
					boolean includeFiles = input.getValueArgument(INCLUDE_FILES);
					
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
								
								if ((includeFiles && r instanceof IFile) || 
									(includeFolders && r instanceof IFolder && ResourcesPlugin.getWorkspace().getRoot() != r)){
									
									result.add(r);
								}
							}
						}
					}
					
					return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(CONTEXT, result));
					
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
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
