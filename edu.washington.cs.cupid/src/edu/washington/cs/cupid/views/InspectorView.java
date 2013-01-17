package edu.washington.cs.cupid.views;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
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
import com.google.common.math.IntMath;
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

/**
 * View that shows capabilities (and their outputs) that apply to the current workbench selection.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class InspectorView extends ViewPart implements IPropertyChangeListener {
	
	//TODO: when a resource is re-selected, use the calculations already in progress (need to keeps a map of jobs?)
	//TODO: add preference for log messages
	
	//TODO: support multiple selections for "local" capabilities
	//TODO: localize status messages
	
	private static final int COLLECTION_PARTITION_SIZE = 10;

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
	
	private SelectionModel selectionModel;

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
		
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(1));
		layout.addColumnData(new ColumnWeightData(2));
		
		tree.setLayout(layout);
		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
			
		viewer = new TreeViewer(tree);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ViewLabelProvider());
		
		selectionModel = new SelectionModel();
		CupidSelectionService.addListener(selectionModel);
		
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
	
	private interface Row {
		boolean hasChildren();
		Row[] getChildren();
		Row getParent();
	    String getColumnText(final Object element, final int columnIndex);
	}
	
	private final class ClassRow implements Row {
		private final Row parent;
		private final String name;
		private final Object value;
		
		public ClassRow(final Row parent, final String name, final Object value) {
			this.parent = parent;
			this.name = name;
			this.value = value;
		}

		@Override
		public boolean hasChildren() {
			return valueHasChildren(value);
		}

		@Override
		public Row[] getChildren() {
			if (value instanceof List) {
				return generateListRows(this, name, (List<?>) value, 0,  ((List<?>) value).size());
			} else if (value.getClass().isArray()) {
				return generateArrayRows(this, name, value, 0, Array.getLength(value));
			} else {
				return generateRows(this, value);
			}
		}

		@Override
		public Row getParent() {
			return parent;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			switch(columnIndex) {
			case 0:
				return name;
			case 1:
				return value == null ? "null" : value.toString();
			default:
				throw new IllegalArgumentException("Invalid column index");
			}
		}	
	}

	private final class ArrayRow implements Row {
		private Row parent;
		private Object array;
		private int offset;
		private int length;
		private String rootName;
		
		public ArrayRow(final Row parent, final String rootName, final Object array, final int offset, final int length) {
			super();
			this.parent = parent;
			this.array  = array;
			this.offset = offset;
			this.rootName = rootName;
			this.length = length;
		}

		@Override
		public boolean hasChildren() {
			return true;
		}

		@Override
		public Row[] getChildren() {
			return generateArrayRows(parent, rootName, array, offset, length);
		}

		@Override
		public Row getParent() {
			return parent;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "[" + offset + " .. " + (offset + length - 1) + "]";
			default:
				return null;
			}
		}
	}
	
	private final class ListRow implements Row {
		private final Row parent;
		private final List<?> list;
		private final int offset;
		private final int length;
		private final String rootName;
		
		public ListRow(final Row parent, final String rootName, final List<?> list, final int offset, final int length) {
			System.out.println("create list row for " + rootName);
			this.parent = parent;
			this.list = list;
			this.offset = offset;
			this.rootName = rootName;
			this.length = length;
		}

		@Override
		public boolean hasChildren() {
			return true;
		}

		@Override
		public Row[] getChildren() {
			return generateListRows(parent, rootName, list, offset, length);
		}

		@Override
		public Row getParent() {
			return parent;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "[" + offset + " .. " + (offset + length - 1) + "]";
			default:
				return null;
			}
		}
	}
	
	private final class CapabilityRow implements Row {
		private final ICapability<?, ?> capability;
		
		private String status = "Updating (submitted)...";
		private boolean interrupted = false;
		private boolean finished = false;
		private IStatus result = null;
		private Object value = null;
		
		private synchronized void updateStatus(final String msg) {
			status = msg;
			update(CapabilityRow.this);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private CapabilityRow(final ICapability capability, final Object input) {

			this.capability = capability;

			// TODO need an Inspector View specific family
			JobFamily family = family(input); 

			synchronized (inspectorFamilies) {
				inspectorFamilies.put(family, System.currentTimeMillis());	
			}

			try {
				CapabilityExecutor.asyncExec(capability, input, family, new IJobChangeListener() {

					@Override
					public synchronized void aboutToRun(final IJobChangeEvent event) {
						updateStatus("Updating (starting)...");
					}

					@Override
					public synchronized void awake(final IJobChangeEvent event) {
						updateStatus("Updating (awake)...");
					}

					@Override
					public synchronized void done(final IJobChangeEvent event) {
						finished = true;
						result = event.getResult();
						value = ((CapabilityStatus) result).value();
						updateStatus("Done");
					}

					@Override
					public synchronized void running(final IJobChangeEvent event) {
						updateStatus("Updating (running)...");
					}

					@Override
					public synchronized void scheduled(final IJobChangeEvent event) {
						updateStatus("Updating (scheduled)...");
					}

					@Override
					public synchronized void sleeping(final IJobChangeEvent event) {
						updateStatus("Updating (sleeping)...");
					}
				});
			} catch (Exception ex) {
				updateStatus("Error running capability: " + ex.getLocalizedMessage());
			}
		}

		@Override
		public synchronized boolean hasChildren() {
			if (finished && result.getCode() == Status.OK) {
				return valueHasChildren(value);
			} else {
				return false;
			}
		}

		@Override
		public synchronized Row[] getChildren() {
			if (finished && result.getCode() == Status.OK) {
				if (value instanceof List) {
					return generateListRows(this, capability.getName(), (List<?>) value, 0, ((List<?>) value).size());
				} else if (value.getClass().isArray()) {
					return generateArrayRows(this, capability.getName(), value, 0, Array.getLength(value));
				} else {
					return generateRows(this, value);
				}
			} else {
				return new Row[]{};
			}
			
		}

		@Override
		public Row getParent() {
			return null;
		}

		@Override
		public synchronized String getColumnText(final Object element, final int columnIndex) {

			switch (columnIndex) {
			case 0:
				return capability.getName();
			case 1:
				if (interrupted) {
					return "Error (interrupted)";
				} else if (finished) {
					switch (result.getCode()) {
					case Status.OK:
						return value.toString();
					case Status.CANCEL:
						return "Update Cancelled...";
					case Status.ERROR:
						return "Exception: " + result.getException().getLocalizedMessage();
					default: 
						throw new RuntimeException("Unexpected plugin-specific status code: " + result.getCode());
					}
				} else {
					return status;
				}
			default:
				return null;
			}
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
			if (element instanceof Row) {
				return ((Row) element).getColumnText(element, columnIndex);
			} else {
				throw new IllegalArgumentException("Illegal table entry");
			}
		}	
	}
	
	private final class SelectionModel implements ICupidSelectionListener {
		@Override
		public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
			synchronized (viewer) {
				cancelOldJobs();
				if (selection instanceof StructuredSelection) {
					// TODO handle multiple data case
					Object argument = ((StructuredSelection) selection).getFirstElement();
					viewer.setInput(argument);
				} 
			}
			
			// TODO handle other selection types
		}

		@Override
		public void selectionChanged(final IWorkbenchPart part, final Object data) {
			synchronized (viewer) {
				if (!part.equals(InspectorView.this)) {
					cancelOldJobs();
					viewer.setInput(data);			
				}
			}
		}

		@Override
		public void selectionChanged(final IWorkbenchPart part, final Object[] data) {
			synchronized (viewer) {
				if (!part.equals(InspectorView.this)) {
					// TODO handle multiple data case
					cancelOldJobs();
					viewer.setInput(data[0]);
				}
			}
		}
	}
	
	@Override
	public final void dispose() {
		super.dispose();
		CupidSelectionService.removeListener(selectionModel);
	}

	private final class ViewContentProvider implements ITreeContentProvider {		
		@Override
		public void inputChanged(final Viewer v, final Object oldSelection, final Object newSelection) {
			// NO OP
		}
		
		@Override
		public void dispose() {
			// NO OP
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
			
			SortedSet<ICapability<?, ?>> capabilities = CupidPlatform.getCapabilityRegistry().getCapabilities(TypeToken.of(argument.getClass()));

			for (ICapability<?, ?> capability : capabilities) {
				if (!hidden.contains(capability.getUniqueId())) {
					Object adapted = TypeManager.getCompatible(capability, argument);
					
					System.out.println("Add row for capability " + capability.getName());
					rows.add(new CapabilityRow(capability, adapted));
					System.out.println("Added row for capability " + capability.getName());
				}
			}

			return rows.toArray(new Object[]{});
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof Row) {
				return ((Row) element).hasChildren();
			}
			return false;
		}
		
		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof Row) {
				return ((Row) parentElement).getChildren();
			} else {
				throw new IllegalArgumentException("Expected row");
			}
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof Row) {
				return ((Row) element).getParent();
			} else {
				return null;
			}
		}	
	}
	
	private boolean valueHasChildren(final Object value) {
		return !(value == null 
			|| value.getClass().isPrimitive() 
			|| value instanceof String 
			|| value instanceof Number);
	}
	
	private Row[] generateArrayRows(final Row parent, final String rootName, final Object array, final int offset, final int length) {
		
		List<Row> result = Lists.newArrayList();
		
		if (length <= COLLECTION_PARTITION_SIZE) {
			for (int i = 0; i < length; i++) {
				result.add(new ClassRow(parent, rootName + "[" + (i + offset) + "]", Array.get(array, offset + i)));
			}
		} else {
			int depth = IntMath.log10(length - 1, RoundingMode.FLOOR);
			int size = IntMath.pow(COLLECTION_PARTITION_SIZE, depth);
			int cnt = IntMath.divide(length, size, RoundingMode.CEILING);
			
			for (int i = 0; i < cnt; i++) {
				int start = i * size;
				int end = Math.min(length - 1, start + size - 1);
				
				result.add(new ArrayRow(parent, rootName, array, offset + start, end - start + 1));
			}
		}
		
		return result.toArray(new Row[]{});
	}

	private Row[] generateListRows(final Row parent, final String rootName, final List<?> value, final int offset, final int length) {
		
		List<Row> result = Lists.newArrayList();
		
		if (length <= COLLECTION_PARTITION_SIZE) {
			for (int i = 0; i < length; i++) {
				result.add(new ClassRow(parent, rootName + "[" + (i + offset) + "]", value.get(i + offset)));
			}
		} else {
			int depth = IntMath.log10(length - 1, RoundingMode.FLOOR);
			int size = IntMath.pow(COLLECTION_PARTITION_SIZE, depth);
			int cnt = IntMath.divide(length, size, RoundingMode.CEILING);
			
			for (int i = 0; i < cnt; i++) {
				int start = i * size;
				int end = Math.min(length - 1, start + size - 1);
				
				result.add(new ListRow(parent, rootName, value, offset + start, end - start + 1));
			}
		}
		
		return result.toArray(new Row[]{});
	}
	
	private Row[] generateRows(final Row parent, final Object value) {
		if (!valueHasChildren(value)) {
			return new Row[]{};
		}
		
		List<Row> children = Lists.newArrayList();
		
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
		
		return children.toArray(new Row[]{});
	}
	
	private JobFamily family(final Object input) {
		return new JobFamily(input, this);
	}
		
	@Override
	public final void setFocus() {
		synchronized (viewer) {
			viewer.getControl().setFocus();
		}
	}
	
	/**
	 * Safely (in the UI thread) updates the given row in the table.
	 * @param row the row to update
	 */
	private void update(final CapabilityRow row) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (viewer) {
					if (!viewer.getTree().isDisposed()) {	
						if (row.finished) {
							viewer.refresh(row);
						} else {
							viewer.update(row, null);
						}
					}
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
