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
package edu.washington.cs.cupid.usage.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

@Entity
public class SessionLog implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Key sessionKey;
	
	private SystemData system;
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<CupidEvent> events;
	
	@SuppressWarnings("unused")
	private SessionLog(){
		this(null, new ArrayList<CupidEvent>());
	}
	
	public SessionLog(SystemData system, List<CupidEvent> events) {
		this.system = system;
		this.events = events;
	}

	public SystemData getSystem() {
		return system;
	}

	public List<CupidEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}

}
