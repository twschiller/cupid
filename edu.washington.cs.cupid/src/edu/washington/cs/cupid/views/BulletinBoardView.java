package edu.washington.cs.cupid.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;

public class BulletinBoardView extends ViewPart  {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.views.BulletinBoardView";

	private TableViewer viewer;
	
	class ViewContentProvider implements IStructuredContentProvider, ICapabilityChangeListener {
		private ICapabilityPublisher publisher;
		
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			
		}
		
		public ViewContentProvider(ICapabilityPublisher publisher) {
			super();
			this.publisher = publisher;
			this.publisher.addChangeListener(this);
		}

		@Override
		public void dispose() {
			publisher.removeChangeListener(this);
		}
		
		@Override
		public Object[] getElements(Object parent) {
			List<String> names = Lists.newArrayList();
		
			for (ICapability<?,?> x : publisher.publish()){
				names.add(x.getName() + ": " + x.getDescription());
			}
			
			return names.toArray(new String[]{});
		}

		@Override
		public void onChange(ICapabilityPublisher publisher) {
			Display.getDefault().asyncExec(new Runnable(){
				@Override
				public void run() {
					viewer.setInput(getViewSite());
				}
			});
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	/**
	 * The constructor.
	 */
	public BulletinBoardView() {		
	}
	
	@Override
	public void createPartControl(Composite parent) {	
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider(CupidPlatform.getCapabilityRegistry()));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}