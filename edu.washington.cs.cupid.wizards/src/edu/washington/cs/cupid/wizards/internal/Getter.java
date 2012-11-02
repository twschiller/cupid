package edu.washington.cs.cupid.wizards.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class Getter<I,V> implements IExtractCapability<I,V>{

	private static final long serialVersionUID = 1L;

	private final static String BASE_ID = "edu.washington.cs.cupid.wizards.internal.getter";
	
	private final TypeToken<I> type;
	private final String field;
	private final TypeToken<V> result;
	
	public Getter(String field, TypeToken<I> type, TypeToken<V> result) {
		super();
		this.field = field;
		this.type = type;
		this.result = result;
	}
	
	@Override
	public String getUniqueId() {
		return BASE_ID + ".[" + type.getRawType().getName() + "]." + field;
	}

	@Override
	public String getName() {
		return field;
	}

	@Override
	public String getDescription() {
		return "Get the '" + field + "' of type " + type.toString();
	}

	@Override
	public TypeToken<I> getParameterType() {
		return type;
	}

	@Override
	public TypeToken<V> getReturnType() {
		return result;
	}

	@Override
	public CapabilityJob<I,V> getJob(final I input) {
		return new CapabilityJob<I,V>(this, input){
			@Override
			protected CapabilityStatus<V> run(IProgressMonitor monitor) {
				try{
					Object out = input.getClass().getMethod(field).invoke(input);
					// TODO check the conversion
					return CapabilityStatus.makeOk((V) out);
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}
			}
		};
	}

	@Override
	public Set<String> getDynamicDependencies() {
		return new HashSet<String>();
	}

	@Override
	public boolean isPure() {
		return true;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	@Override
	public boolean isTransient() {
		return true;
	}
}
