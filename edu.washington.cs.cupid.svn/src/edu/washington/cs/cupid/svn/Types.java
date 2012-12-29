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
