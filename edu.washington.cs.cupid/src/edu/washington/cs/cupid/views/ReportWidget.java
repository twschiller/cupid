package edu.washington.cs.cupid.views;

import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.jobs.NullJobListener;

public class ReportWidget extends Composite {

	private ICapability capability;
	private TableViewer viewer;
	private Table table;
	private ReportComparator comparator;
	
	public ReportWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());
	}

	public class ReportRow {
		public final String inputName;
		public final Object input;
		public CapabilityStatus result;
		public boolean finished;
		
		public ReportRow(String inputName, Object input) {
			this.inputName = inputName;
			this.input = input;
			this.finished = false;
		}
		
		public void load(){
			ICapability.IParameter<?> param = CapabilityUtil.unaryParameter(capability);
			Object compatible = TypeManager.getCompatible(param, input);
			ICapabilityArguments arg = CapabilityUtil.packUnaryInput(capability, compatible);
			CapabilityExecutor.asyncExec(capability, arg, ReportWidget.this, new NullJobListener(){
				@Override
				public void done(IJobChangeEvent event) {
					result = (CapabilityStatus) event.getResult();
					finished = true;
					update(ReportRow.this);
				}
			});
		}
	}
		
	public void init(final ICapability capability, final Object[] data){
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (!display.isDisposed()){
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					ReportWidget.this.capability = capability;
					buildTable(capability, ReportWidget.this);
					setData(data);
				}
			});
		}
	}
	
	public void setCapability(final ICapability capability){
		if (this.capability != capability){
			this.capability = capability;
			
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (!display.isDisposed()){
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						buildTable(capability, ReportWidget.this);
					}
				});
			}
		}
	}
	
	private void buildTable(ICapability capability, Composite root){
		if (viewer != null){
			table.dispose();
			viewer = null;
			table = null;
		}
	
		viewer = new TableViewer(this, SWT.V_SCROLL | SWT.H_SCROLL);
		table = viewer.getTable();
		
		viewer.setContentProvider(new ArrayContentProvider());
		
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		comparator = new ReportComparator();
		viewer.setComparator(comparator);
		
		TableViewerColumn input = new TableViewerColumn(viewer, SWT.NONE);
		input.getColumn().setText(CapabilityUtil.unaryParameter(capability).getName());
		input.getColumn().setWidth(200);
		input.setLabelProvider(new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				return ((ReportRow) element).inputName;
			}
		});
		
		for (final ICapability.IOutput<?> output: capability.getOutputs()){
			TableViewerColumn cOutput = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = cOutput.getColumn();
			
			column.setText(output.getName());
			column.setMoveable(true);
			column.setWidth(200);
			cOutput.setLabelProvider(new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					CapabilityStatus status = ((ReportRow) element).result;
					if (status != null){
						
						if (status.isOK() && status.getException() == null){
							Object result = status.value().getOutput(output);
							return result.toString();
						}else{
							return status.getMessage();
						}
					}else{
						return  "";
					}
				}
			});
			
			column.addSelectionListener(getSelectionAdapter(column, output));
		}
		
		root.layout(true);
	}
	
	private String firstLine(String original){
		if (original == null || original.equals("")){
			return original;
		}else{
			return original.split(System.getProperty("line.separator"))[0];
		}
	}
	
	public void setData(Object [] data){
		if (capability == null) return;
		ICapability.IParameter<?> param = CapabilityUtil.unaryParameter(capability);
		
		List<ReportRow> rows = Lists.newArrayList();
		for (Object x : data){
			if (TypeManager.isCompatible(param, x)){
				rows.add(new ReportRow(firstLine(x.toString()), x));
			}
		}
		
		if (!rows.isEmpty()){
			if (viewer == null) throw new IllegalStateException("Viewer not initialized");
			viewer.setInput(rows);
			
			for (ReportRow row : rows){
				row.load();
			}
		}
	}

	public class ReportComparator extends ViewerComparator {
		// adapted from http://www.vogella.com/articles/EclipseJFaceTableAdvanced/article.html#jfacetable_tablecolumnlayout 
		private static final int DESCENDING = 1;
		
		private ICapability.IOutput<?> output;
		private int direction = DESCENDING;
	
		public ReportComparator() {
			this.output = null;
			direction = DESCENDING;
		}

		public int getDirection(){
			return direction;
		}
		
		public void setOutput(ICapability.IOutput<?> output) {
			if (output == this.output) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.output = output;
				direction = DESCENDING;
			}
		}
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			ReportRow r1 = (ReportRow) e1;
			ReportRow r2 = (ReportRow) e2;
			int rc = 0;
			
			if (r1.finished && r2.finished){
				Object v1 = r1.result.value().getOutput(output);
				Object v2 = r2.result.value().getOutput(output);
				
				if (v1 instanceof Comparable && v2 instanceof Comparable){
					rc = ((Comparable) v1).compareTo(v2);
				}else{
					rc = 0;
				}
			}else if (r1.finished){
				rc = -1;
			}else{
				rc = 1;
			}
			
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}
	
	private SelectionAdapter getSelectionAdapter(final TableColumn column, final ICapability.IOutput<?> output) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setOutput(output);
				int dir = comparator.getDirection();
				table.setSortDirection(dir);
				table.setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}
	

	/**
	 * Safely (in the UI thread) updates the given row in the table.
	 * @param row the row to update
	 */
	private void update(final ReportRow row) {
		Display display = PlatformUI.getWorkbench().getDisplay();

		// does this need synchronization?
		if (!display.isDisposed()){
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!table.isDisposed()) {	
						if (row.finished) {
							viewer.refresh(row);
						} else {
							viewer.update(row, null);
						}
					}
				}
			});
		}	
	}
}
