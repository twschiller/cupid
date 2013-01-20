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

import java.io.Serializable;

/**
 * A capability that can be serialized (e.g., saved to disk and restored).
 * @author Todd Schiller
 * @param <I> input type
 * @param <V> output type
 */
public interface ISerializableCapability<I, V> extends ICapability<I, V>, Serializable {

}
