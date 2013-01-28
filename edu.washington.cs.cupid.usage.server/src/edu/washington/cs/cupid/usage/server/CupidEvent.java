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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

/**
 * The {@link CupidEvent} class captures information about a single event. Adapted from
 * Eclipse's {@link UsageDataEvent} class.
 * @author Todd Schiller
 */
@Embeddable
public class CupidEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String what;
	
	private final String kind;
	
	@OneToMany(cascade = CascadeType.ALL) 
	private final Map<String, Serializable> data;
	
	private final String bundleId;
	
	private String bundleVersion;
	
	private final long when;

	@SuppressWarnings("unused")
	private CupidEvent(){
		this(null, null, new HashMap<String, Serializable>(), null, null, -1L);
	}
	
	public CupidEvent(String what, String kind, Map<String, Serializable> data, String bundleId, String bundleVersion, long when) {
		this.what = what;
		this.kind = kind;
		this.data = data;
		this.bundleId = bundleId;
		this.bundleVersion = bundleVersion;
		this.when = when;
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

	public Map<String, Serializable> getData() {
		return Collections.unmodifiableMap(data);
	}

	public String getBundleId() {
		return bundleId;
	}

	public long getWhen() {
		return when;
	}
}
