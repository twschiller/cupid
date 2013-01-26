/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.wizards.internal;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public final class Getter<I,V> implements IExtractCapability<I,V>{

	private static final long serialVersionUID = 1L;

	private static final String BASE_ID = "edu.washington.cs.cupid.wizards.internal.getter";
	
	private final TypeToken<I> type;
	private final String field;
	private final TypeToken<V> result;
	
	public Getter(final String field, final TypeToken<I> type, final TypeToken<V> result) {
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
	public CapabilityJob<I, V> getJob(final I input) {
		return new CapabilityJob<I,V>(this, input){
			@Override
			protected CapabilityStatus<V> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					
					Method method = input.getClass().getMethod(field);
					
					if (!method.isAccessible()) {
						method.setAccessible(true);
					}
					
					Object out = method.invoke(input);
					// TODO check the conversion
					return CapabilityStatus.makeOk((V) out);
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
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
