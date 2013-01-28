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
import java.util.Map;

/**
 * The {@link JsonCupidEvent} class captures information about a single event. Adapted from
 * Eclipse's {@link UsageDataEvent} class.
 * @author Todd Schiller
 */
public final class JsonCupidEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The {@link #what} field describes the event that has occurred. It
	 * is dependent on the kind of thing that caused the event. As a 
	 * rule of thumb, the value indicates that something has
	 * already happened (e.g. "activated", "loaded", "clicked").
	 */
	public String what;
	
	/**
	 * The {@link #kind} field describes the kind of thing that caused
	 * the event (e.g. "view", "workbench", "menu", "bundle").
	 */
	public String kind;
	
	public Map<String, String> data;
	
	/**
	 * The {@link #bundleId} field contains symbolic name of the bundle that
	 * owns the thing that caused the event.
	 */
	public String bundleId;
	
	/**
	 * The {@link #bundleVersion} field contains the version of the bundle
	 * that owns the thing that caused the event.
	 */
	public String bundleVersion;
	
	/**
	 * The {@link #when} field contains a time stamp, expressed as
	 * milliseconds in UNIX time (using <code>System.currentTimeMillis()</code>);
	 */
	public long when;

	private JsonCupidEvent(){
		// NO OP
	}	
}
