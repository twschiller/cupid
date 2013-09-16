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
package edu.washington.cs.cupid.junit;

import java.util.Set;

import org.eclipse.jdt.internal.junit.model.TestElement;

import com.google.common.reflect.TypeToken;

/**
 * Types used by the JUnit Cupid plugin.
 * @author Todd Schiller
 */
@SuppressWarnings("restriction")
public final class Types {
	
	/**
	 * Token for a set of {@link TestElement}s.
	 */
	public static final TypeToken<Set<TestElement>> TEST_ELEMENTS = new TypeToken<Set<TestElement>>() {
		private static final long serialVersionUID = 1L;
	};
	
	private Types() {
		// NO OP
	}
}
