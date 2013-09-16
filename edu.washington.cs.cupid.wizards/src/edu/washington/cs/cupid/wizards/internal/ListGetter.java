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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.exception.TypeException;
import edu.washington.cs.cupid.capability.linear.GenericLinearSerializableCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public final class ListGetter<I,V> extends GenericLinearSerializableCapability<List<I>,List<V>> implements IExtractCapability<List<I>,List<V>>{
	private static final long serialVersionUID = 3L;
	
	private final TypeToken<I> type;
	private final String field;
	private final TypeToken<V> result;
	
	public ListGetter(final String field, final TypeToken<I> type, final TypeToken<V> result) {
		super("[ " + field + " ]", 
			  "Get the '" + field + "' of type " + TypeManager.simpleTypeName(type),
			  Flag.PURE);
			 	  
		this.field = field;
		this.type = type;
		this.result = result;
	}
	

	@Override
	public LinearJob<List<I>, List<V>> getJob(final List<I> input) {
		return new LinearJob<List<I>, List<V>>(this, input) {
			@Override
			protected LinearStatus<List<V>> run(final IProgressMonitor monitor) {
				
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
					
					return LinearStatus.makeOk(getCapability(), result);
				} catch (Exception ex) {
					return LinearStatus.<List<V>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}


	@Override
	public TypeToken<List<I>> getInputType() {
		return new TypeToken<List<I>>(getClass()){}.where(new TypeParameter<I>(){}, type);
	}


	@Override
	public TypeToken<List<V>> getOutputType() {
		return new TypeToken<List<V>>(getClass()){}.where(new TypeParameter<V>(){}, result);
	}
}
