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

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public final class Getter<I, V> extends LinearCapability<I, V> implements IExtractCapability<I,V> {

	private static final long serialVersionUID = 1L;

	private static final String BASE_ID = "edu.washington.cs.cupid.wizards.internal.getter";
	
	private final String field;
	
	public Getter(final String field, final TypeToken<I> type, final TypeToken<V> result) {
		
		super(field, "Get the '" + field + "' of type " + type.toString(),
			  BASE_ID + ".[" + type.getRawType().getName() + "]." + field,
			  type, result,
			  Flag.PURE);
					
		this.field = field;
	}
	
	public String getField() {
		return field;
	}
	
	@Override
	public LinearJob<I, V> getJob(final I input) {
		return new LinearJob<I,V>(this, input){
			@Override
			protected LinearStatus<V> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					
					Method method = input.getClass().getMethod(field);
					
					if (!method.isAccessible()) {
						method.setAccessible(true);
					}
					
					Object out = method.invoke(input);
					// TODO check the conversion
					return LinearStatus.makeOk(getCapability(), (V) out);
				} catch (Exception ex) {
					return LinearStatus.<V>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
