package edu.washington.cs.cupid.utility;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Sets;

/**
 * Aggregates {@link IResource} subtrees with leaf nodes with specified flags
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class ResourceDeltaVisitor implements IResourceDeltaVisitor {

	private final Set<IResource> matches = Sets.newHashSet();
	private final int flags;
	
	public ResourceDeltaVisitor(int flags){
		this.flags = flags;
	}
		
	public Set<IResource> getMatches() {
		return matches;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		if ((delta.getFlags() & flags) != 0){
			addAll(delta.getResource());
		}
		return true;
	}
	
	private void addAll(IResource resource){
		if (resource != null){
			matches.add(resource);
			addAll(resource.getParent());
		}
	}
}
