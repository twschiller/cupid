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

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability.Flag;
import edu.washington.cs.cupid.capability.linear.GenericLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public final class SetGetter<I,V> extends GenericLinearCapability<Set<I>,Set<V>> implements IExtractCapability<Set<I>,Set<V>> {
	private static final long serialVersionUID = 1L;

	private static final String BASE_ID = "edu.washington.cs.cupid.wizards.internal.set.getter";
	
	private final TypeToken<I> type;
	private final String field;
	private final TypeToken<V> result;
	
	public SetGetter(final String field, final TypeToken<I> type, final TypeToken<V> result) {
		super("{ " + field + " }", "Get the '" + field + "' of type " + TypeManager.simpleTypeName(type.getType()),
			  BASE_ID + "." + field,
			  Flag.PURE);
		
		this.field = field;
		this.type = type;
		this.result = result;
	}
	
	@Override
	public LinearJob<Set<I>, Set<V>> getJob(final Set<I> input) {
		return new LinearJob<Set<I>, Set<V>>(this, input) {
			@Override
			protected LinearStatus<Set<V>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), input.size());
					
					Set<V> result = Sets.newHashSet();
					
					for (I x : input) {
						Method method = x.getClass().getMethod(field);
						
						if (!method.isAccessible()) {
							method.setAccessible(true);
						}
						
						Object out = method.invoke(x);
						// TODO check the conversion
						result.add((V) out);
						
						monitor.worked(1);
					}
					
					return LinearStatus.makeOk(getCapability(), result);
				} catch (Exception ex) {
					return LinearStatus.<Set<V>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
	

	@Override
	public TypeToken<Set<I>> getInputType() {
		return new TypeToken<Set<I>>(getClass()){}.where(new TypeParameter<I>(){}, type);
	}


	@Override
	public TypeToken<Set<V>> getOutputType() {
		return new TypeToken<Set<V>>(getClass()){}.where(new TypeParameter<V>(){}, result);
	}

}
