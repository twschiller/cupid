package edu.washington.cs.cupid.mylyn;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class InActiveContextCapability extends LinearCapability<IResource, Boolean> {

	public InActiveContextCapability() {
		super("In Active Task Context", "True if the resource is in the active task context", 
			  IResource.class, Boolean.class, 
			  ICapability.Flag.PURE);
	}

	@Override
	public LinearJob<IResource, Boolean> getJob(IResource input) {
		return new LinearJob<IResource, Boolean>(this, input){

			@Override
			protected LinearStatus<Boolean> run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					IInteractionContext context = ContextCorePlugin.getContextManager().getActiveContext();
					
					if (context == null){
						return LinearStatus.makeOk(getCapability(), false);
					}
					
					for (IInteractionElement x : context.getAllElements()){
						if (x.getContentType().equals("resource")){
							IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(x.getHandleIdentifier());
							if (r != null && r.equals(getInput())){
								return LinearStatus.makeOk(getCapability(), true);
							}
						}
					}
					
					return LinearStatus.makeOk(getCapability(), false);
					
				} catch (Exception ex) {
					return LinearStatus.<Boolean>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
