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

/**
 * Interface for classes that provide a set of Cupid capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public interface ICapabilityPublisher extends ICapabilityChangeNotifier {
	
	/**
	 * Returns the current set available capabilities. Capabilities are referentially stable.
	 * @return the available capabilities
	 */
	ICapability<?, ?> [] publish();
}
