package edu.washington.cs.cupid.views;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
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
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.jobs.JobFamily;
import edu.washington.cs.cupid.preferences.PreferenceConstants;
import edu.washington.cs.cupid.preferences.SelectionInspectorPreferencePage;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.select.ICupidSelectionListener;
import edu.washington.cs.cupid.utility.CapabilityUtil;

/**
 * View that shows capabilities (and their outputs) that apply to the current workbench selection.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class InspectorView extends ViewPart implements IPropertyChangeListener {
	
	//TODO: when a resource is re-selected, use the calculations already in progress (need to keeps a map of jobs?)
	//TODO: add preference for log messages
	
	//TODO: support multiple selections for "local" capabilities
	//TODO: localize status messages
	
	private static final int MILLISECONDS_PER_SECOND = 1000;

	private static final int DEFAULT_COLUMN_WIDTH = 100;

	private static final boolean INCLUDE_NULL_OUTPUT = false;
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.views.InspectorView";
	
	private SashForm sash;
	
	private TreeViewer viewer;
	private Text detail;
	
	private ViewContentProvider contentProvider;
		
	private long reapThresholdInSeconds;
	
	/**
	 * Family -> Last Job Submit Time. Used smooth killing of jobs when clicking around the UI
	 */
	private Map<JobFamily, Long> inspectorFamilies;
	
	@Override
	public final void createPartControl(final Composite parent) {
		contentProvider = new ViewContentProvider();
		
		sash = new SashForm(parent, SWT.BORDER | SWT.HORIZONTAL);
		Tree tree = new Tree(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
		detail = new Text(sash, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		
		TreeColumn cCapability = new TreeColumn(tree, SWT.LEFT);
		cCapability.setText("Capability");
		cCapability.setWidth(DEFAULT_COLUMN_WIDTH);
		
		TreeColumn cValue = new TreeColumn(tree, SWT.LEFT);
		cValue.setText("Value");
		cValue.setWidth(DEFAULT_COLUMN_WIDTH);
		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
			
		viewer = new TreeViewer(tree);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ViewLabelProvider());
		
		CupidSelectionService.addListener(contentProvider);
		
		inspectorFamilies = Maps.newHashMap();
		
		IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
		reapThresholdInSeconds = preferences.getInt(PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS);
		preferences.addPropertyChangeListener(this);
		
		viewer.addSelectionChangedListener(new DetailContentProvider());
	}
	
	private class DetailContentProvider implements ISelectionChangedListener {
		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			
			detail.setText("");
		
			if (selection instanceof ITreeSelection) {
				Object row = ((ITreeSelection) selection).getFirstElement();
				if (row instanceof ClassRow) {
					detail.setText(((ClassRow) row).value.toString());
				} else if (row instanceof CapabilityRow) {
					CapabilityRow x = (CapabilityRow) row;
					if (x.finished && x.result.getCode() == Status.OK) {
						detail.setText(x.value.toString());
					}
				}
			}
		}
	}
	
	/**
	 * Cancel families of jobs that have not been accessed in longer than
	 * {@link #reapThresholdInSeconds}.
	 */
	private void cancelOldJobs() {
		synchronized (inspectorFamilies) {
			List<Object> cancel = Lists.newLinkedList();
			for (Object key : inspectorFamilies.keySet()) {
				long last = inspectorFamilies.get(key);
				if (System.currentTimeMillis() - last > (reapThresholdInSeconds * MILLISECONDS_PER_SECOND)) {
					cancel.add(key);
				}
			}
			
			for (Object family : cancel) {
				Job.getJobManager().cancel(family);
				inspectorFamilies.remove(family);
				System.out.println("Cancelling family " + family);
			}
		}
	}
	
	
	private final class ClassRow {
		private Object parent;
		private String name;
		private Object value;
		
		public ClassRow(final Object parent, final String name, final Object value) {
			this.parent = parent;
			this.name = name;
			this.value = value;
		}
	}
	
	private final class CapabilityRow {
		private ICapability<?, ?> capability;
		
		private String status = "Updating (submitted)...";
		private boolean interrupted = false;
		private boolean finished = false;
		private IStatus result = null;
		private Object value = null;
		
		private void updateStatus(final String msg) {
			synchronized (this) {
				status = msg;
				update(CapabilityRow.this);
			}
		}
		
//		private <I> ICapability<I, ?> formCapability(final ICapability<I, ?> original) {
//			ICapability<?, String> viewAdapter = CupidPlatform.getCapabilityRegistry().getViewer(original.getReturnType());
//			
//			if (viewAdapter != null) {
//				return new TransientPipeline(
//						original.getName(), original.getDescription(), 
//						Lists.newArrayList(original, viewAdapter));
//			} else {
//				return (ICapability<I, String>) original;
//			}
//		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private CapabilityRow(final ICapability capability, final Object input) {
			this.capability = capability;
			
			// TODO need an Inspector View specific family
			JobFamily family = family(input); 
			
			synchronized (inspectorFamilies) {
				inspectorFamilies.put(family, System.currentTimeMillis());	
			}
			
			System.out.println("Keep alive family " + family);
			
			CapabilityExecutor.asyncExec(capability, input, family, new IJobChangeListener() {

				@Override
				public void aboutToRun(final IJobChangeEvent event) {
					updateStatus("Updating (starting)...");
				}

				@Override
				public void awake(final IJobChangeEvent event) {
					updateStatus("Updating (awake)...");
				}

	
				@Override
				public void done(final IJobChangeEvent event) {
					synchronized (this) {
						finished = true;
						result = event.getResult();
						value = ((CapabilityStatus) result).value();
						updateStatus("Done");
					}
				}

				@Override
				public void running(final IJobChangeEvent event) {
					updateStatus("Updating (running)...");
				}

				@Override
				public void scheduled(final IJobChangeEvent event) {
					updateStatus("Updating (scheduled)...");
				}

				@Override
				public void sleeping(final IJobChangeEvent event) {
					updateStatus("Updating (sleeping)...");
				}
			});
		}
	}
	
	private final class ViewLabelProvider implements ITableLabelProvider {

		@Override
		public void dispose() {
			// NO OP
		}

		@Override
		public boolean isLabelProperty(final Object element, final String property) {
			return false;
		}

		@Override
		public void addListener(final ILabelProviderListener listener) {
			// NO OP
		}
		
		@Override
		public void removeListener(final ILabelProviderListener listener) {
			// NO OP
		}

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			if (element instanceof CapabilityRow) {

				CapabilityRow row = (CapabilityRow) element;

				switch (columnIndex) {
				case 0:
					return row.capability.getName();
				case 1:
					if (row.interrupted) {
						return "Error (interrupted)";
					} else if (row.finished) {
						switch (row.result.getCode()) {
						case Status.OK:
							return row.value.toString();
						case Status.CANCEL:
							return "Update Cancelled...";
						case Status.ERROR:
							return "Exception: " + row.result.getException().getLocalizedMessage();
						default: 
							throw new RuntimeException("Unexpected plugin-specific status code: " + row.result.getCode());
						}
					} else {
						return row.status;
					}
				default:
					throw new IllegalArgumentException("Invalid column index");
				}
			} else if (element instanceof ClassRow) {
				ClassRow row = (ClassRow) element;
				switch(columnIndex) {
				case 0:
					return row.name;
				case 1:
					return row.value == null ? "null" : row.value.toString();
				default:
					throw new IllegalArgumentException("Invalid column index");
				}
				
			} else {
				throw new IllegalArgumentException("Illegal table entry");
			}
		}	
	}
	
	private final class ViewContentProvider implements ITreeContentProvider, ICupidSelectionListener {		
		@Override
		public void inputChanged(final Viewer v, final Object oldSelection, final Object newSelection) {
			v.refresh();
		}
		
		@Override
		public void dispose() {
			CupidSelectionService.removeListener(this);
		}
	
		@Override
		public Object[] getElements(final Object argument) {
			Set<String> hidden = Sets.newHashSet(
					CupidActivator.getDefault().getPreferenceStore().getString(
							PreferenceConstants.P_INSPECTOR_HIDE).split(SelectionInspectorPreferencePage.SEPARATOR));

			if (argument == null) {
				return new Object[]{};
			}

			List<Object> rows = Lists.newArrayList();
			
			Set<ICapability<?, ?>> capabilities = CupidPlatform.getCapabilityRegistry().getCapabilities(TypeToken.of(argument.getClass()));

			for (ICapability<?, ?> capability : CapabilityUtil.sort(capabilities, CapabilityUtil.COMPARE_NAME)) {
				if (!hidden.contains(capability.getUniqueId())) {
					
					Object adapted = TypeManager.getCompatible(capability, argument);
					rows.add(new CapabilityRow(capability, adapted));
				}
			}

			return rows.toArray(new Object[]{});
		}

		@Override
		public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
			cancelOldJobs();
			if (selection instanceof StructuredSelection) {
				// TODO handle multiple data case
				Object argument = ((StructuredSelection) selection).getFirstElement();
				viewer.setInput(argument);
			} 
			
			// TODO handle other selection types
		}

		@Override
		public void selectionChanged(final IWorkbenchPart part, final Object data) {
			if (!part.equals(InspectorView.this)) {
				cancelOldJobs();
				viewer.setInput(data);			
			}
		}

		@Override
		public void selectionChanged(final IWorkbenchPart part, final Object[] data) {
			if (!part.equals(InspectorView.this)) {
				// TODO handle multiple data case
				cancelOldJobs();
				viewer.setInput(data[0]);
			}
		}

		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof CapabilityRow) {
				CapabilityRow row = (CapabilityRow) parentElement;
				
				if (row.finished && row.result.getCode() == Status.OK) {
					return generateRows(row, row.value);
				}
			} else if (parentElement instanceof ClassRow) {
				ClassRow row = (ClassRow) parentElement;
				return generateRows(row, row.value);
			}
			
			return new Object[] {};
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof ClassRow) {
				return ((ClassRow) element).parent;
			} else {
				return null;
			}
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof CapabilityRow) {
				CapabilityRow row = (CapabilityRow) element;
				if (row.finished && row.result.getCode() == Status.OK) {
					return valueHasChildren(row.value);
				}
			} else if (element instanceof ClassRow) {
				return valueHasChildren(((ClassRow) element).value);
			}
			
			return false;
		}
	}
	
	private boolean valueHasChildren(final Object value) {
		return !(value == null 
			|| value.getClass().isPrimitive() 
			|| value instanceof String 
			|| value instanceof Number);
	}
	
	private Object[] generateRows(final Object parent, final Object value) {
		if (value == null) {
			return new Object[]{};
		} else if (value instanceof String) {
			return new Object[]{};
		}
		
		List<ClassRow> children = Lists.newArrayList();
		
		for (Field field : value.getClass().getFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				try {
					Object child = field.get(value);
					children.add(new ClassRow(parent, field.getName(), child));
				} catch (Exception e) {
					children.add(new ClassRow(parent, field.getName(), e));
				}
			}
		}
		
		for (Method method : value.getClass().getMethods()) {
			if (!Modifier.isStatic(method.getModifiers())) {
				if (method.getParameterTypes().length == 0 
					&& method.getName().startsWith("get")
					&& !method.getName().equalsIgnoreCase("getClass")) {
					
					try {
						Object child = method.invoke(value);
						
						if (child != null || INCLUDE_NULL_OUTPUT) {
							children.add(new ClassRow(parent, method.getName(), child));
						}
					} catch (Exception e) {
						children.add(new ClassRow(parent, method.getName(), e));
					}
				}
			}
		}
		
		return children.toArray();
	}
	
	private JobFamily family(final Object input) {
		return new JobFamily(input, this);
	}
		
	@Override
	public final void setFocus() {
		viewer.getControl().setFocus();
	}
	
	/**
	 * Safely (in the UI thread) updates the given row in the table.
	 * @param row the row to update
	 */
	private void update(final CapabilityRow row) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!viewer.getTree().isDisposed()) {	
					viewer.refresh(row);
				}
			}
		});
	}

	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS)) {
			reapThresholdInSeconds = (Integer) event.getNewValue();
		}
	}
	
}
