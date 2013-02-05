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
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.exception.TypeException;

public final class ListGetter<I,V> implements IExtractCapability<List<I>,List<V>>{
	private static final long serialVersionUID = 1L;

	private static final String BASE_ID = "edu.washington.cs.cupid.wizards.internal.list.getter";
	
	private final TypeToken<I> type;
	private final String field;
	private final TypeToken<V> result;
	
	public ListGetter(final String field, final TypeToken<I> type, final TypeToken<V> result) {
		this.field = field;
		this.type = type;
		this.result = result;
	}
	
	@Override
	public String getUniqueId() {
		return BASE_ID + "." + field;
	}

	@Override
	public String getName() {
		return "[ " + field + " ]";
	}

	@Override
	public String getDescription() {
		return "Get the '" + field + "' of type " + type.toString();
	}

	@SuppressWarnings("serial") // not serializable since it contains a type variable
	@Override
	public TypeToken<List<I>> getParameterType() {
		return new TypeToken<List<I>>(getClass()) {}.where(new TypeParameter<I>(){}, type);
	}

	@SuppressWarnings("serial") // not serializable since it contains a type variable
	public TypeToken<List<V>> getReturnType() {
		return new TypeToken<List<V>>(getClass()) {}.where(new TypeParameter<V>(){}, result);
	}

	@Override
	public CapabilityJob<List<I>, List<V>> getJob(final List<I> input) {
		return new CapabilityJob<List<I>, List<V>>(this, input) {
			@SuppressWarnings("unchecked") // FP: cast of result to V is checked
			@Override
			protected CapabilityStatus<List<V>> run(final IProgressMonitor monitor) {
				
				try {
					monitor.beginTask(getName(), input.size());
					
					List<V> result = Lists.newArrayList();
					
					for (I x : input) {
						Method method = x.getClass().getMethod(field);
					
						if (!method.isAccessible()) {
							method.setAccessible(true);
						}
						
						if (ListGetter.this.result.isAssignableFrom(method.getGenericReturnType())) {
							Object out = method.invoke(x);
							result.add((V) out);
						} else {
							throw new TypeException(TypeToken.of(method.getGenericReturnType()), ListGetter.this.result);
						}
						
						monitor.worked(1);
					}
					
					return CapabilityStatus.makeOk(result);
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
