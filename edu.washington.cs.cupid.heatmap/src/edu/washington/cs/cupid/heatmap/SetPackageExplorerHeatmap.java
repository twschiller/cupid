package edu.washington.cs.cupid.heatmap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;

@SuppressWarnings("restriction")
public class SetPackageExplorerHeatmap extends AbstractHandler{
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PackageExplorerPart explorer = PackageExplorerPart.getFromActivePerspective();
		TreeViewer tree = explorer.getTreeViewer();
		String id = event.getParameter("cupid.heatmap.pipeline");
		
		if (id == null){
			tree.setLabelProvider(new JavaUILabelProvider());
			//DecoratingJavaLabelProvider x = (DecoratingJavaLabelProvider) tree.getLabelProvider();
			
		}else{
			ICapability pipeline;
			try {
				pipeline = CupidPlatform.getCapabilityRegistry().findCapability(id);
			} catch (NoSuchCapabilityException e) {
				e.printStackTrace(System.err);
				return null;
			}
			
			
			DecoratingJavaLabelProvider labelProvider = (DecoratingJavaLabelProvider) tree.getLabelProvider();
			tree.setLabelProvider(new HeatMapLabelProvider((JavaUILabelProvider) labelProvider.getStyledStringProvider(), pipeline));
	
			System.out.println("Activating heatmap pipeline " + id);		
		}
		return null;
	}

}
