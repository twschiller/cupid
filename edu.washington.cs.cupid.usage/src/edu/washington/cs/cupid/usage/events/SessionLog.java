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
package edu.washington.cs.cupid.usage.events;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class SessionLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private SystemData system;
	private List<CupidEvent> events;
	
	public SessionLog(SystemData system, List<CupidEvent> events) {
		this.system = system;
		this.events = Lists.newArrayList(events);
	}

	public SystemData getSystem() {
		return system;
	}

	public List<CupidEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}
}
