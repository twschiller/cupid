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
package edu.washington.cs.cupid.svn;

import java.util.List;

import org.tmatesoft.svn.core.SVNLogEntry;

import com.google.common.reflect.TypeToken;

/**
 * TypeTokens used by the SVN plugin
 * @author Todd Schiller
 */
public abstract class Types {

	public static final TypeToken<List<SVNLogEntry>> SVN_LOG = new TypeToken<List<SVNLogEntry>>(){
		private static final long serialVersionUID = 1L;
	};
	
}
