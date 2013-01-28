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
package edu.washington.cs.cupid.usage.internal;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.usage.events.CupidEvent;

public final class SessionLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private String uuid;
	private SystemData system;
	private List<CupidEvent> events;
	
	public SessionLog(String uuid, SystemData system, List<CupidEvent> events) {
		this.uuid = uuid;
		this.system = system;
		this.events = Lists.newArrayList(events);
	}
	
	public String getUUID() {
		return uuid;
	}

	public SystemData getSystem() {
		return system;
	}

	public List<CupidEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}
}
