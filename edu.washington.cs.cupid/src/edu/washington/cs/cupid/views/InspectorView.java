package edu.washington.cs.cupid.views;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.LinearPipeline;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.jobs.JobFamily;
import edu.washington.cs.cupid.preferences.PreferenceConstants;
import edu.washington.cs.cupid.preferences.SelectionInspectorPreferencePage;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.select.ICupidSelectionListener;

/**
 * View that shows capabilities (and their outputs) that apply to the current workbench selection.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class InspectorView extends ViewPart implements IPropertyChangeListener {
	
	//TODO: when a resource is re-selected, use the calculations already in progress (need to keeps a map of jobs?)
	//TODO: add preference for log messages
	
	//TODO: support multiple selections for "local" capabilities
	//TODO: localize status messages
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.views.InspectorView";
	
	private TableViewer viewer;
	
	private ViewContentProvider contentProvider;
		
	private long reapThresholdInSeconds;
	
	/**
	 * Family -> Last Job Submit Time. Used smooth killing of jobs when clicking around the UI
	 */
	private ConcurrentMap<JobFamily, Long> inspectorFamilies;
	
	@Override
	public void createPartControl(Composite parent) {
		contentProvider = new ViewContentProvider();
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		viewer.setContentProvider(contentProvider);
		
		TableViewerColumn cCapability = new TableViewerColumn(viewer, SWT.NONE);
		cCapability.getColumn().setText("Capability");
		cCapability.getColumn().setWidth(100);
		cCapability.setLabelProvider(new CapabilityColumnProvider());
		
		TableViewerColumn cValue = new TableViewerColumn(viewer, SWT.NONE);
		cValue.getColumn().setText("Value");
		cValue.getColumn().setWidth(100);
		cValue.setLabelProvider(new ValueColumnProvider());
		
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		CupidSelectionService.addListener(contentProvider);
		
		inspectorFamilies = Maps.newConcurrentMap();
		
		IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
		reapThresholdInSeconds = preferences.getInt(PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS);
		preferences.addPropertyChangeListener(this);
		
		//viewer.setInput(selectionService.getSelection());
	}
	
	/**
	 * Cancel families of jobs that have not been accessed in longer than
	 * {@link #reapThresholdInSeconds}
	 */
	private void cancelOldJobs(){
		synchronized(inspectorFamilies){
			List<Object> cancel = Lists.newLinkedList();
			for (Object key : inspectorFamilies.keySet()){
				long last = inspectorFamilies.get(key);
				if (System.currentTimeMillis() - last > (reapThresholdInSeconds * 1000)){
					cancel.add(key);
				}
			}
			
			for (Object family : cancel){
				Job.getJobManager().cancel(family);
				inspectorFamilies.remove(family);
				System.out.println("Cancelling family " + family);
			}
		}
	}
	
	/**
	 * The model for a row in the table
	 * @author Todd Schiller (tws@cs.washington.edu)
	 * @param <I> the capabilty's input type
	 * @param <V> the result of the capability (or intermediate status)
	 */
	private class InspectorRow<I,V>{
		private ICapability<I,V> capability;
		
		private String status = "Updating (submitted)...";
		private boolean interrupted = false;
		private boolean finished = false;
		private IStatus result = null;
		private V value = null;
		
		private void updateStatus(String msg){
			synchronized(this){
				status = msg;
				update(InspectorRow.this);
			}
		}
		
		private ICapability<I,?> formCapability(ICapability<I,V> original){
			ICapability<?,String> viewer = CupidPlatform.getCapabilityRegistry().getViewer(original.getReturnType());
			
			if (viewer != null){
				return new LinearPipeline(original.getName(), original.getDescription(), original, viewer);
			}else{
				return original;
			}
		}
		
		public InspectorRow(ICapability<I,V> capability, I input) {
			this.capability = capability;
			
			JobFamily family = family(input); // TODO need an Inspector View specific family
			inspectorFamilies.put(family, System.currentTimeMillis());
			System.out.println("Keep alive family " + family);
			
			CapabilityExecutor.asyncExec(formCapability(capability), input, family, new IJobChangeListener(){

				@Override
				public void aboutToRun(IJobChangeEvent event) {
					updateStatus("Updating (starting)...");
				}

				@Override
				public void awake(IJobChangeEvent event) {
					updateStatus("Updating (awake)...");
				}

				@SuppressWarnings("unchecked")
				@Override
				public void done(IJobChangeEvent event) {
					synchronized(this){
						finished = true;
						result = event.getResult();
						value = ((CapabilityStatus<V>) result).value();
						updateStatus("Done");
					}
				}

				@Override
				public void running(IJobChangeEvent event) {
					updateStatus("Updating (running)...");
				}

				@Override
				public void scheduled(IJobChangeEvent event) {
					updateStatus("Updating (scheduled)...");
				}

				@Override
				public void sleeping(IJobChangeEvent event) {
					updateStatus("Updating (sleeping)...");
				}
			});
		}
	}
	
	private class ViewContentProvider implements IStructuredContentProvider, ICupidSelectionListener {
		private Object selection;
		private TableViewer view;
		
		@Override
		public void inputChanged(Viewer v, Object oldSelection, Object newSelection) {
			selection = newSelection;
			view = (TableViewer) v;
			v.refresh();
		}
		
		@Override
		public void dispose() {
			CupidSelectionService.removeListener(this);
		}
	
		@Override
		public Object[] getElements(Object argument) {
			Set<String> hidden = Sets.newHashSet(
					CupidActivator.getDefault().getPreferenceStore().getString(
							PreferenceConstants.P_INSPECTOR_HIDE).split(SelectionInspectorPreferencePage.SEPARATOR));

			if (argument == null){
				return new Object[]{};
			}

			List<Object> rows = Lists.newArrayList();

			for (ICapability<?,?> capability : CupidPlatform.getCapabilityRegistry().getCapabilities(TypeToken.of(argument.getClass()))){
				if (!hidden.contains(capability.getUniqueId())){
					
					Object adapted = TypeManager.getCompatible(capability, argument);
					rows.add(new InspectorRow(capability, adapted));
				}
			}

			return rows.toArray(new Object[]{});
		}

		@Override
		public void selectionChanged(IWorkbenchPart part, final ISelection selection) {
			cancelOldJobs();
			if (selection instanceof StructuredSelection){
				// TODO handle multiple data case
				Object argument = ((StructuredSelection) selection).getFirstElement();
				viewer.setInput(argument);
			}else{
				// TODO handle other selection types
			}
		}

		@Override
		public void selectionChanged(IWorkbenchPart part, Object data) {
			if (!part.equals(InspectorView.this)){
				cancelOldJobs();
				viewer.setInput(data);			
			}
		}

		@Override
		public void selectionChanged(IWorkbenchPart part, Object[] data) {
			if (!part.equals(InspectorView.this)){
				// TODO handle multiple data case
				cancelOldJobs();
				viewer.setInput(data[0]);
			}
		}
	}
	
	private JobFamily family(Object input){
		return new JobFamily(input, this);
	}
	
	private class CapabilityColumnProvider extends ColumnLabelProvider{
		@Override
		public String getText(Object element) {
			return super.getText(((InspectorRow<?,?>) element).capability.getName());
		}
	}
	
	private class ValueColumnProvider extends ColumnLabelProvider{
		@Override
		public String getText(Object element) {
			InspectorRow<?,?> row = (InspectorRow<?,?>) element;
			if (row.interrupted){
				return "Error (interrupted)";
			}else if (row.finished){
				switch (row.result.getCode()){
				case Status.OK:
					return super.getText(row.value);
				case Status.CANCEL:
					return "Update Cancelled...";
				case Status.ERROR:
					return "Exception: " + row.result.getException().getLocalizedMessage();
				default: 
					throw new RuntimeException("Unexpected plugin-specific status code: " + row.result.getCode());
				}
			}else{
				return row.status;
			}
		}
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	/**
	 * Safely (in the UI thread) updates the given row in the table
	 * @param row the row to update
	 */
	private void update(final InspectorRow<?,?> row){
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				viewer.update(row, null);
			}
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS)){
			reapThresholdInSeconds = (Integer) event.getNewValue();
		}
	}
	
}
