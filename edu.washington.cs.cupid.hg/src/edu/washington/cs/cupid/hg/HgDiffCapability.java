package edu.washington.cs.cupid.hg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.compare.contentmergeviewer.TokenComparator;
import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.vectrace.MercurialEclipse.commands.HgClients;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.compare.HgDifferencer;
import com.vectrace.MercurialEclipse.compare.RevisionNode;
import com.vectrace.MercurialEclipse.history.MercurialRevision;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.FileFromChangeSet;
import com.vectrace.MercurialEclipse.model.FileStatus;
import com.vectrace.MercurialEclipse.model.HgFile;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.model.HgWorkspaceFile;
import com.vectrace.MercurialEclipse.model.JHgChangeSet;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

import edu.washington.cs.cupid.capability.ICapability.Flag;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class HgDiffCapability extends LinearCapability<IFile, List<ILineRange>> {

	/**
	 * Construct a capability that returns the Hg heads for a resource.
	 */
	public HgDiffCapability() {
		super("Hg Diff",
			  "Hg diff for current file",
			  TypeToken.of(IFile.class), new TypeToken<List<ILineRange>>(){}, 
			  Flag.PURE);
	}
	
	@Override
	public LinearJob<IFile, List<ILineRange>> getJob(final IFile input) {
		return new LinearJob<IFile, List<ILineRange>>(this, input) {
			@Override
			protected LinearStatus<List<ILineRange>> run(final IProgressMonitor monitor) {
				try {
					
					int revision = -1;
					monitor.beginTask(getName(), 100);
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(input);
					
					if (root == null) {
						return LinearStatus.<List<ILineRange>>makeError(new ResourceNotHgVersionedException(input));
					}
					
					ChangeSet changeSet = revision < 0 ? 
							HgLogClient.getChangeSet(root, HgLogClient.getCurrentChangeset(root))
							: HgLogClient.getChangeSet(root, revision);
							
					monitor.worked(50);
							

					
					List<ILineRange> result = Lists.newArrayList();
					
					if (changeSet.contains(input)){
						for (FileFromChangeSet f : changeSet.getChangesetFiles()){
							if (f.getPath().equals(input.getProjectRelativePath())){	
					            HgWorkspaceFile left = HgWorkspaceFile.make(f.getFile());
					            HgFile right = HgFile.make((JHgChangeSet) changeSet, f.getFile());
							
					            result= doDiff(left, right, new SubProgressMonitor(monitor, 50));
					            break;
							}
						}	
					}
					
					return LinearStatus.makeOk(getCapability(), result);
				} catch (Exception ex) {
					return LinearStatus.<List<ILineRange>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
		
	} 
		
	private List<ILineRange> doDiff(HgWorkspaceFile current, HgFile rev, IProgressMonitor monitor) throws CoreException{
		List<ILineRange> result = Lists.newArrayList();
		
	    RevisionNode leftNode = new RevisionNode(current);
        RevisionNode rightNode = new RevisionNode(rev);
		
		Document cc = new Document(getStringFromInputStream(leftNode.getContents()));
		Document revc = new Document(getStringFromInputStream(rightNode.getContents()));
	
        RangeDifference[] diffs = RangeDifferencer.findDifferences(
        		new DocLineComparator(cc, null, false), 
        		new DocLineComparator(revc, null, false));
        
        
        for (RangeDifference d : diffs){
        	result.add(new LineRange(d.leftStart(), d.leftLength()));
        }
        return result;
	}
	
	// convert InputStream to String
		private static String getStringFromInputStream(InputStream is) {
	 
			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();
	 
			String line;
			try {
	 
				br = new BufferedReader(new InputStreamReader(is));
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
	 
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	 
			return sb.toString();
	 
		}

}
