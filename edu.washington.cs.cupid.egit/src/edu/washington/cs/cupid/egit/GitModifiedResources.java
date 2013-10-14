package edu.washington.cs.cupid.egit;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class GitModifiedResources extends LinearCapability<RevCommit, List<IResource>> {
	public GitModifiedResources() {
		super( "Git Modified Files",
			   "Get the files modified in the input revision",
			   TypeToken.of(RevCommit.class), new TypeToken<List<IResource>>(){},
			   ICapability.Flag.PURE);	
	}

	public static Set<Repository> getAllWorkspaceRepositories(IProgressMonitor monitor){
		return Sets.newHashSet(org.eclipse.egit.core.Activator.getDefault().getRepositoryCache().getAllRepositories());
	}
	
	
	public static List<IResource> getChangeSet(Repository repository, RevCommit commit) throws IOException{
		RevWalk rw = new RevWalk(repository);
		
		// must look up by ID, otherwise the parent will be just for the file.
		RevCommit real = rw.parseCommit(commit.getId());
		RevCommit parent = rw.parseCommit(real.getParent(0).getId());
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = df.scan(parent.getTree(), real.getTree());
		
		List<IResource> result = Lists.newArrayList();
		
		for (DiffEntry diff : diffs) {
			String path = diff.getNewPath();
			IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			
			if (r != null && (diff.getChangeType() == ChangeType.MODIFY)){
				result.add(r);
			}
		}
		return result;
	}

	@Override
	public LinearJob<RevCommit, List<IResource>> getJob(RevCommit input) {
		return new LinearJob<RevCommit, List<IResource>>(this, input) {
			@Override
			protected LinearStatus<List<IResource>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
					
					Set<Repository> repositories = getAllWorkspaceRepositories(new SubProgressMonitor(monitor, 50));
					
					for (Repository r : repositories){
						try {
							List<IResource> x = getChangeSet(r, getInput());
							return LinearStatus.makeOk(GitModifiedResources.this, x);
						}catch (Exception ex){
							// NOP
						}
					}
					
					throw new RuntimeException("No matching repository found");
				} catch (Exception e) {
					return LinearStatus.<List<IResource>>makeError(e);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
