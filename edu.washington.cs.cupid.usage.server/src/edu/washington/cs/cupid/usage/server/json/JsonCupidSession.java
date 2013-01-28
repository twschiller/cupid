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
package edu.washington.cs.cupid.usage.server.json;

import java.io.Serializable;
import java.util.List;

public final class JsonCupidSession implements Serializable {
	private static final long serialVersionUID = 1L;

	public String uuid;
	public JsonSystemData system;
	public List<JsonCupidEvent> events;
	
}
