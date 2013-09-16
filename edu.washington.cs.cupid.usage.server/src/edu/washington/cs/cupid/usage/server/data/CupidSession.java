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
package edu.washington.cs.cupid.usage.server.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;

@Entity
public class CupidSession implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key sessionKey;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="uuid")
	@Unowned
	private CupidUser user;
	
	@Embedded
	private SystemData system;
	
	@OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="session")
	private List<CupidEvent> events;
	
	public CupidSession(CupidUser user, SystemData system){
		this.user = user;
		this.system = system;
		events = new ArrayList<CupidEvent>();
	}

	public void addEvent(CupidEvent event){
		events.add(event);
	}
	
	public Key getSessionKey() {
		return sessionKey;
	}
	
	public CupidUser getUser() {
		return user;
	}

	public SystemData getSystem() {
		return system;
	}

	public List<CupidEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}

}
