package edu.washington.cs.cupid.jdt.compiler;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractBaseCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.OutputBuilder;
import edu.washington.cs.cupid.capability.Parameter;

public class StackFrameResourceCapability extends AbstractBaseCapability {

	public static final IParameter<IJavaStackFrame> PARAM_STACK_FRAME = new Parameter<IJavaStackFrame>("Stack Frame", IJavaStackFrame.class);
	public static final Output<IResource> OUT_RESOURCE = new Output<IResource>("Resource", TypeToken.of(IResource.class));
	public static final Output<Integer> OUT_LINE_NUMBER = new Output<Integer>("Line Number", TypeToken.of(Integer.class));
	
	public StackFrameResourceCapability() {
		super("Stack Frame Resource",
			  "Returns resource associated with the given stack frame",
				  Lists.<IParameter<?>>newArrayList(PARAM_STACK_FRAME),
				  Lists.<Output<?>>newArrayList(OUT_RESOURCE, OUT_LINE_NUMBER),
				  Flag.PURE);
	}

	@Override
	public CapabilityJob<StackFrameResourceCapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<StackFrameResourceCapability> (this, input){
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);

					IJavaStackFrame stackFrame = input.getValueArgument(PARAM_STACK_FRAME);
					
					IJavaReferenceType type = stackFrame.getReferenceType();
					List<IResource> found = Lists.newArrayList();
					
					for (IJavaProject proj : JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects()){
					
						for (String path : type.getSourcePaths(type.getDefaultStratum())){
							IJavaElement match = proj.findElement(new Path(path));
							if (match != null && match.getCorrespondingResource() != null){
								found.add(match.getCorrespondingResource());
							}
						}
					}
					
					if (found.isEmpty()){
						throw new RuntimeException("No corresponding resource found for stack frame");
					}else if (found.size() == 1){
						
						OutputBuilder output = new OutputBuilder(StackFrameResourceCapability.this);
						output.add(OUT_RESOURCE, found.get(0));
						output.add(OUT_LINE_NUMBER, stackFrame.getLineNumber());
					
						return CapabilityStatus.makeOk(output.getOutputs());
					}else{
						throw new RuntimeException("Multiple resources found for stack frame");
					}
					
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
