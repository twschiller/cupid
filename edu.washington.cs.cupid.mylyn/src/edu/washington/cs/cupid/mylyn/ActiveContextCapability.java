package edu.washington.cs.cupid.mylyn;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class ActiveContextCapability extends LinearCapability<Void, IInteractionContext>{

	public ActiveContextCapability() {
		super("Mylyn Active Task Context", "The Context for the Active Task", 
			  TypeToken.of(Void.class), TypeToken.of(IInteractionContext.class),
			  ICapability.Flag.PURE);
	}

	@Override
	public LinearJob<Void, IInteractionContext> getJob(Void input) {
		return new LinearJob<Void, IInteractionContext>(this, input) {
			@Override
			protected LinearStatus<IInteractionContext> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					IInteractionContext context = ContextCorePlugin.getContextManager().getActiveContext();
					
					if (context == null){
						throw new RuntimeException("No active task context");
					}
					
					return LinearStatus.makeOk(getCapability(), context);
				} catch (Exception ex) {
					return LinearStatus.<IInteractionContext>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
