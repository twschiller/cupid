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
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * The {@link CupidEvent} class captures information about a single event. Adapted from
 * Eclipse's {@link UsageDataEvent} class.
 * @author Todd Schiller
 */
public final class CupidEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The {@link #what} field describes the event that has occurred. It
	 * is dependent on the kind of thing that caused the event. As a 
	 * rule of thumb, the value indicates that something has
	 * already happened (e.g. "activated", "loaded", "clicked").
	 */
	private String what;
	
	/**
	 * The {@link #kind} field describes the kind of thing that caused
	 * the event (e.g. "view", "workbench", "menu", "bundle").
	 */
	private String kind;
	
	private Map<String, String> data;
	
	/**
	 * The {@link #bundleId} field contains symbolic name of the bundle that
	 * owns the thing that caused the event.
	 */
	private String bundleId;
	
	/**
	 * The {@link #bundleVersion} field contains the version of the bundle
	 * that owns the thing that caused the event.
	 */
	private String bundleVersion;
	
	/**
	 * The {@link #when} field contains a time stamp, expressed as
	 * milliseconds in UNIX time (using <code>System.currentTimeMillis()</code>);
	 */
	private long when;

	@SuppressWarnings("unused")
	private CupidEvent(){
		this(null, null, new HashMap<String, String>(), null, null, -1L);
	}
	
	public CupidEvent(String what, String kind, Map<String, String> data, String bundleId, String bundleVersion, long when) {
		this.what = what;
		this.kind = kind;
		this.data = Maps.newHashMap(data);
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
