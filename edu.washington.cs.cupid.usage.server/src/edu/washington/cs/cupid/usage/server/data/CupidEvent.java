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
import java.util.Collections;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.datanucleus.api.jpa.annotations.Extension;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;

/**
 * The {@link CupidEvent} class captures information about a single event. Adapted from
 * Eclipse's {@link UsageDataEvent} class.
 * @author Todd Schiller
 */
@Entity
public class CupidEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="uuid")
	@Unowned
	private CupidUser user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="sessionKey")
	@Unowned
	private CupidSession session;
	
	private String what;
	
	private String kind;
	
	@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
	private Map<String, String> data;
	
	private String bundleId;
	
	private String bundleVersion;
	
	private long when;

	public CupidEvent(CupidUser user, CupidSession session, String what,
			String kind, Map<String, String> data, String bundleId,
			String bundleVersion, long when) {
		this.user = user;
		this.session = session;
		this.what = what;
		this.kind = kind;
		this.data = data;
		this.bundleId = bundleId;
		this.bundleVersion = bundleVersion;
		this.when = when;
	}

	public Key getKey() {
		return key;
	}

	public CupidUser getUser() {
		return user;
	}

	public CupidSession getSession() {
		return session;
	}
	
	public String getBundleVersion() {
		return bundleVersion;
	}

	public void setBundleVersion(String bundleVersion) {
		this.bundleVersion = bundleVersion;
	}

	public String getWhat() {
		return what;
	}

	public String getKind() {
		return kind;
	}

	public Map<String, String> getData() {
		return Collections.unmodifiableMap(data);
	}

	public String getBundleId() {
		return bundleId;
	}

	public long getWhen() {
		return when;
	}
}
