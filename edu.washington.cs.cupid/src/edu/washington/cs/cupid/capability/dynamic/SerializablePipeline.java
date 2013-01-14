package edu.washington.cs.cupid.capability.dynamic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;

/**
 * A linear pipeline with dynamic binding that can be serialized.
 * @param <I> input type
 * @param <V> output type
 * @author Todd Schiller
 */
@SuppressWarnings("rawtypes")
public class SerializablePipeline<I, V> extends AbstractSerializableCapability<I, V> {
	// TODO handle concurrent modifications to capability bindings
	
	private static final long serialVersionUID = 1L;
	
	private final List<Serializable> capabilities;

	/**
	 * Construct a serializable pipeline of capabilities.
	 * @param name the capability name
	 * @param description the capability description
	 * @param capabilities the pipeline of capabilities
	 */
	public SerializablePipeline(final String name, final String description, final List<Serializable> capabilities) {
		super(name, description, capabilities);
		this.capabilities = Lists.newArrayList(capabilities);
	}

	private List<ICapability<?, ?>> inorder() throws NoSuchCapabilityException {
		Map<String, ICapability<?, ?>> map = super.current();
		
		List<ICapability<?, ?>> result = Lists.newArrayList();
	
		for (Object capability : capabilities) {
			if (capability instanceof ICapability) {
				result.add((ICapability) capability);
			} else if (capability instanceof String) {
				result.add(map.get((String) capability));
			} else {
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
		}
		return result;
	}
	
	@Override
	public final String getUniqueId() {
		StringBuilder builder = new StringBuilder();
		builder.append("[pipe:");
		for (Object capability : capabilities) {
			if (capability instanceof ICapability) {
				builder.append(((ICapability) capability).getUniqueId());
			} else if (capability instanceof String) {
				builder.append(((String) capability));
			} else {
				throw new RuntimeException("Unexpected pipeline element of type " + capability.getClass().getName());
			}
			
			builder.append(";");
		}
		builder.append("]");
		return builder.toString();	
	}

	private ICapability<?, ?> get(final int index) throws NoSuchCapabilityException {
		return get(capabilities.get(index));
	}

	@SuppressWarnings("unchecked")
	@Override
	public final TypeToken<I> getParameterType() {
		try {
			return (TypeToken<I>) get(0).getParameterType();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final TypeToken<V> getReturnType() {
		try {
			return (TypeToken<V>) get(capabilities.size() - 1).getReturnType();
		} catch (NoSuchCapabilityException e) {
			throw new DynamicBindingException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final CapabilityJob getJob(final Object input) {
		return new CapabilityJob(this, input) {
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				Object result = getInput();
				
				monitor.beginTask(this.getName(), SerializablePipeline.this.capabilities.size());

				List<ICapability<?, ?>> resolved;
				try {
					resolved = inorder();
				} catch (NoSuchCapabilityException e) {
					return CapabilityStatus.makeError(e);
				}
				
				List<Object> intermediateResults = Lists.newArrayList();
				intermediateResults.add(result);

				for (ICapability capability : resolved) {
					if (monitor.isCanceled()) {
						return CapabilityStatus.makeCancelled();
					}

					CapabilityJob<?, ?> subtask = capability.getJob(result);
					
					if (subtask == null) {
						throw new RuntimeException("Capability " + capability.getName() + " produced null job");
					}
					
					monitor.subTask(subtask.getName());
					subtask.schedule();
					try {
						subtask.join();
					} catch (InterruptedException e) {
						monitor.done();
						return CapabilityStatus.makeError(e);
					}

					CapabilityStatus status = ((CapabilityStatus) subtask.getResult());

					if (status.getCode() == Status.OK) {
						result = status.value();
						intermediateResults.add(result);
						monitor.worked(1);
					} else {
						monitor.done();
						return CapabilityStatus.makeError(status.getException());
					}
				}

				monitor.done();
				return CapabilityStatus.makeOk(result);
			}
		};
	}
}
