package edu.washington.cs.cupid.hg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.compare.RevisionNode;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.FileFromChangeSet;
import com.vectrace.MercurialEclipse.model.HgFile;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.model.JHgChangeSet;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import edu.washington.cs.cupid.capability.AbstractBaseCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityOutputs;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.OptionalParameter;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.Parameter;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

public class HgDiffCapability extends AbstractBaseCapability  {

	public static final TypeToken<List<ILineRange>> LINE_CHANGE_TYPE = new TypeToken<List<ILineRange>>(){};
	
	public static final IParameter<ITextFileBuffer> PARAM_BUFFER = new Parameter<ITextFileBuffer>("Text Buffer", ITextFileBuffer.class);
	public static final IParameter<Integer> PARAM_REVISION = new OptionalParameter<Integer>("Revision", Integer.class, -1);
	
	public static final Output<List<ILineRange>> OUT_ADDED = new Output<List<ILineRange>>("Lines Added", LINE_CHANGE_TYPE);
	public static final Output<List<ILineRange>> OUT_MODIFIED = new Output<List<ILineRange>>("Lines Modified", LINE_CHANGE_TYPE);
	public static final Output<List<ILineRange>> OUT_ALL = new Output<List<ILineRange>>("Lines Add/Modified", LINE_CHANGE_TYPE);
	
	/**
	 * Construct a capability that returns the Hg heads for a resource.
	 */
	public HgDiffCapability() {
		super("Hg Diff",
			  "Hg diff for current file",
			  Lists.<IParameter<?>>newArrayList(PARAM_BUFFER, PARAM_REVISION),
			  Lists.<Output<?>>newArrayList(OUT_ADDED, OUT_MODIFIED, OUT_ALL),
			  Flag.PURE);
	}
	
	@Override
	public CapabilityJob<HgDiffCapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<HgDiffCapability>(this, input) {
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try {
					ITextFileBuffer buffer = input.getValueArgument(PARAM_BUFFER);
					int revision = input.getValueArgument(PARAM_REVISION);
					
					monitor.beginTask(getName(), 100);
					
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(buffer.getLocation());
				
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(file);
					
					if (root == null) {
						return LinearStatus.<List<ILineRange>>makeError(new ResourceNotHgVersionedException(file));
					}
					
					ChangeSet changeSet = revision < 0 ? 
							HgLogClient.getChangeSet(root, HgLogClient.getCurrentChangeset(root))
							: HgLogClient.getChangeSet(root, revision);
							
					monitor.worked(50);
												
					HgDiff result = new HgDiff();
					
					if (changeSet.contains(file)){
						for (FileFromChangeSet f : changeSet.getChangesetFiles()){
							if (f.getPath().equals(file.getProjectRelativePath())){	
					            HgFile right = HgFile.make((JHgChangeSet) changeSet, f.getFile());
							
					            result= doDiff(buffer, right, new SubProgressMonitor(monitor, 50));
					            break;
							}
						}	
					}
					
					CapabilityOutputs outputs = new CapabilityOutputs();
					outputs.add(OUT_ADDED, result.added);
					outputs.add(OUT_MODIFIED, result.modified);
					
					List<ILineRange> all = Lists.newArrayList();
					all.addAll(result.added);
					all.addAll(result.modified);
					
					outputs.add(OUT_ALL, all);
					
					return  CapabilityStatus.makeOk(outputs);
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
		
	} 
		
	private static class HgDiff {
		private final List<ILineRange> added = Lists.newArrayList();
		private final List<ILineRange> modified = Lists.newArrayList();
	}
	
	private HgDiff doDiff(ITextFileBuffer current, HgFile rev, IProgressMonitor monitor) throws CoreException{
		
		HgDiff result = new HgDiff();
		
	    RevisionNode rightNode = new RevisionNode(rev);
		
		Document revc = new Document(getStringFromInputStream(rightNode.getContents()));
	
        RangeDifference[] diffs = RangeDifferencer.findDifferences(
        		new DocLineComparator(current.getDocument(), null, false), 
        		new DocLineComparator(revc, null, false));
        
        for (RangeDifference d : diffs){
        	if (d.rightLength() == 0){
        		result.added.add(new LineRange(d.leftStart(), d.leftLength()));
        	}else{
        		result.modified.add(new LineRange(d.leftStart(), d.leftLength()));
        	}
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
