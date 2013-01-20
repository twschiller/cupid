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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class SetGetter<I,V> implements IExtractCapability<Set<I>,Set<V>>{
	private static final long serialVersionUID = 1L;

	private final static String BASE_ID = "edu.washington.cs.cupid.wizards.internal.set.getter";
	
	private final TypeToken<I> type;
	private final String field;
	private final TypeToken<V> result;
	
	public SetGetter(String field, TypeToken<I> type, TypeToken<V> result) {
		super();
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
		return "{ " + field + " }";
	}

	@Override
	public String getDescription() {
		return "Get the '" + field + "' of type " + type.toString();
	}

	@Override
	public TypeToken<Set<I>> getParameterType() {
		return new TypeToken<Set<I>>(){}.where(new TypeParameter<I>(){}, type);
	}

	@Override
	public TypeToken<Set<V>> getReturnType() {
		return new TypeToken<Set<V>>(){}.where(new TypeParameter<V>(){}, result);
	}

	@Override
	public CapabilityJob<Set<I>,Set<V>> getJob(final Set<I> input) {
		return new CapabilityJob<Set<I>,Set<V>>(this, input){
			@Override
			protected CapabilityStatus<Set<V>> run(IProgressMonitor monitor) {
				Set<V> result = Sets.newHashSet();
				
				try{
					for (I x : input){
						Object out = x.getClass().getMethod(field).invoke(x);
						// TODO check the conversion
						result.add((V) out);
					}
					
					return CapabilityStatus.makeOk(result);
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
