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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearSerializableCapability;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public final class Getter<I, V> extends LinearSerializableCapability<I, V> implements IExtractCapability<I,V> {

	private static final long serialVersionUID = 2L;

	private static final String BASE_ID = "edu.washington.cs.cupid.wizards.internal.getter";
	
	private final List<String> fields;
	
	public Getter(final String field, final TypeToken<I> type, final TypeToken<V> result) {
		
		super(field, BASE_ID + ".[" + type.getRawType().getName() + "]." + field,
			  "Get the '" + field + "' of type " + TypeManager.simpleTypeName(type),
			  type, result,
			  Flag.PURE);
					
		this.fields = Lists.newArrayList(field);
	}
	
	
	public Getter(final List<String> fields, final TypeToken<I> type, final TypeToken<V> result) {
		
		super(TypeManager.simpleTypeName(type) + "." + Joiner.on(".").join(fields), 
			  BASE_ID + ".[" + type.getRawType().getName() + "]." + Joiner.on(".").join(fields),
			  TypeManager.simpleTypeName(type) + "." + Joiner.on(".").join(fields),
			  type, result,
			  Flag.PURE);
					
		this.fields = fields;
	}
	
	
	@Override
	public LinearJob<I, V> getJob(final I input) {
		return new LinearJob<I, V>(this, input){
			@Override
			protected LinearStatus<V> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), fields.size());
					
					Object result = getInput();
					
					for (String field : fields){
						Method method = result.getClass().getMethod(field);
						if (!method.isAccessible()) {
							method.setAccessible(true);
						}
						result = method.invoke(result);		
						monitor.worked(1);
					}
					// TODO check the conversion
					return LinearStatus.makeOk(getCapability(), (V) result);
				} catch (Exception ex) {
					return LinearStatus.<V>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
	
	public List<String> getFields(){
		return Lists.newArrayList(fields);
	}
	

}
