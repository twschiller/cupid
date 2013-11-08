package edu.washington.cs.cupid.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.select.ICupidSelectionListener;

public class ReportView extends ViewPart implements ICupidSelectionListener  {
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.views.ReportView";
	
	private Composite root;
	private ReportWidget report;
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		CupidSelectionService.addListener(this);
		super.init(site);
	}

	@Override
	public void dispose() {
		CupidSelectionService.removeListener(this);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		root = parent;
		root.setLayout(new GridLayout());
		report = new ReportWidget(parent, SWT.NONE);
		report.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	@Override
	public void setFocus() {
		// NOP
	}
	
	public void setCapability(final ICapability capability){
		report.setCapability(capability);
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, Object[] data) {
		if (part != this){
			report.setData(data);	
		}		
	}
}
