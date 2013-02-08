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
package edu.washington.cs.cupid.capability;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability.IOutput;

public class Output<I> implements IOutput<I> {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private TypeToken<I> type;
	
	public Output(String name, TypeToken<I> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeToken<I> getType() {
		return type;
	}

	
}
