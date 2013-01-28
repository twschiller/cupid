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

import javax.persistence.Embeddable;

@Embeddable
public final class SystemData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String locale;
	private String os;
	private String ws;
	private String osArch;
	private String vmName;
	private String vmVendor;
	private String vmVersion;
	
	@SuppressWarnings("unused")
	private SystemData(){
		// NO OP
	}
	
	public SystemData(String locale, String os, String osArch, 
			String ws, String vmName, String vmVendor, String vmVersion) {
		this.locale = locale;
		this.os = os;
		this.ws = ws;
		this.osArch = osArch;
		this.vmName = vmName;
		this.vmVendor = vmVendor;
		this.vmVersion = vmVersion;
	}

	public String getLocale() {
		return locale;
	}

	public String getOs() {
		return os;
	}

	public String getWs() {
		return ws;
	}

	public String getOsArch() {
		return osArch;
	}

	public String getVmName() {
		return vmName;
	}

	public String getVmVendor() {
		return vmVendor;
	}

	public String getVmVersion() {
		return vmVersion;
	}
}
