/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.views;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.math.IntMath;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityArguments;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IOutput;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.jobs.JobFamily;
import edu.washington.cs.cupid.preferences.PreferenceConstants;
import edu.washington.cs.cupid.preferences.SelectionInspectorPreferencePage;
import edu.washington.cs.cupid.select.CupidSelectionService;
import edu.washington.cs.cupid.select.ICupidSelectionListener;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;
import edu.washington.cs.cupid.usage.events.EventConstants;

/**
 * View that shows capabilities (and their outputs) that apply to the current workbench selection.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class InspectorView extends ViewPart {
	
	//TODO: support multiple selections for "local" capabilities
	//TODO: localize status messages
	
	private static final int COLLECTION_PARTITION_SIZE = 10;


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

	private Set<JobFamily> oldJobs = Sets.newIdentityHashSet();
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		CupidDataCollector.record(
				new CupidEventBuilder(EventConstants.LOADED_WHAT, getClass(), CupidActivator.getDefault())
				.create());
		
		super.init(site);
	}

	@Override
	public final void createPartControl(final Composite parent) {
		contentProvider = new ViewContentProvider();
		
		sash = new SashForm(parent, SWT.BORDER | SWT.HORIZONTAL);
		Tree tree = new Tree(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
		detail = new Text(sash, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		
		TreeColumn cCapability = new TreeColumn(tree, SWT.LEFT);
		cCapability.setText("Capability");
		
		TreeColumn cValue = new TreeColumn(tree, SWT.LEFT);
		cValue.setText("Value");
		
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
		
		viewer.addSelectionChangedListener(new DetailContentProvider());
	}
	
	private void cancelOldJobs(){
		CapabilityExecutor.getJobManager().cancel(oldJobs);
		oldJobs.clear();
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
					if (x.finished && x.status.getCode() == Status.OK) {
						
						if (x.value == null){
							throw new NullPointerException("Capability '" + x.capability.getName() + "' returned null");
						}
						
						detail.setText(x.value.toString());
					}
				}
			}
		}
	}
		
	private interface Row {
		boolean hasChildren();
		Row[] getChildren();
		Row getParent();
	    String getColumnText(final Object element, final int columnIndex);
	}
	
	private final class OutputRow implements Row{
		private final CapabilityRow parent;
		private final ICapability.IOutput<?> output;
		private final Object value;
		
		public OutputRow(CapabilityRow parent, IOutput<?> output, Object value) {
			super();
			this.parent = parent;
			this.output = output;
			this.value = value;
		}

		@Override
		public boolean hasChildren() {
			return valueHasChildren(value);
		}

		@Override
		public Row[] getChildren() {
			if (value instanceof List) {
				return generateListRows(this, output.getName(), (List<?>) value, 0, ((List<?>) value).size());
			} else if (value.getClass().isArray()) {
				return generateArrayRows(this, output.getName(), value, 0, Array.getLength(value));
			} else {
				return generateRows(this, value);
			}		
		}

		@Override
		public Row getParent() {
			return parent;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return output.getName();
			case 1:
				if (value == null) {
					return "null";
				} else if (value instanceof Exception){
					String msg = ((Exception) value).getLocalizedMessage();
					return msg == null ? "<error>" : ("<error:" + msg + ">");  
				} else {
					return value.toString();
				}
			default:
				throw new IllegalArgumentException("Invalid column index");
			}
		}
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
				if (value == null) {
					return "null";
				} else if (value instanceof Exception){
					String msg = ((Exception) value).getLocalizedMessage();
					return msg == null ? "<error>" : ("<error:" + msg + ">");  
				} else {
					return value.toString();
				}
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
		private final ICapability capability;
		
		private String statusMsg = "Updating (submitted)...";
		private boolean interrupted = false;
		private boolean finished = false;
		private CapabilityStatus status = null;
		private Object value = null;
		
		private synchronized void updateStatus(final String msg) {
			statusMsg = msg;
			update(CapabilityRow.this);
		}
		
		private CapabilityRow(final ICapability capability, final Object input) {

			this.capability = capability;
			JobFamily family = family(input);
			
			oldJobs.add(family);
			CapabilityExecutor.getJobManager().register(family);
			
			try {
				ICapabilityArguments args = CapabilityUtil.isGenerator(capability)
						? new CapabilityArguments()
						: CapabilityUtil.packUnaryInput(capability, input);

				CapabilityExecutor.asyncExec(capability, args, family, new IJobChangeListener() {

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
						synchronized(CapabilityRow.this){
							finished = true;
							status = (CapabilityStatus) event.getResult();
						
							if (CapabilityUtil.hasSingleOutput(capability)){
								value = CapabilityUtil.singleOutputValue(capability, status);		
							}else{
								value = status.value();
							}
							
							updateStatus("Done");
						}
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
				updateStatus("Error running capability (" + ex.getClass().getSimpleName() + "): " + ex.getMessage());
			}
		}

		@Override
		public synchronized boolean hasChildren() {
			if (finished && status.getCode() == Status.OK) {
				return valueHasChildren(value);
			} else {
				return false;
			}
		}

		@Override
		public synchronized Row[] getChildren() {
			if (finished && status.getCode() == Status.OK) {
				
				if (CapabilityUtil.hasSingleOutput(capability)){
					if (value instanceof List) {
						return generateListRows(this, capability.getName(), (List<?>) value, 0, ((List<?>) value).size());
					} else if (value.getClass().isArray()) {
						return generateArrayRows(this, capability.getName(), value, 0, Array.getLength(value));
					} else {
						return generateRows(this, value);
					}		
				}else{
					return generateOutputRows(this, status);
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
					switch (status.getCode()) {
					case Status.OK:
						if (value != null){
							if (capability.getOutputs().size() > 1){
								return "<Multiple Outputs>"; 
							}else{
								return value.toString();			
							}
						} else {
							return null;
						}
					case Status.CANCEL:
						return "Update Cancelled...";
					case Status.ERROR:
						return "Exception: " + status.getException().getLocalizedMessage();
					default: 
						throw new RuntimeException("Unexpected plugin-specific status code: " + status.getCode());
					}
				} else {
					return statusMsg;
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
		public void selectionChanged(final IWorkbenchPart part, final Object[] data) {
			synchronized (viewer) {
				if (!part.equals(InspectorView.this)) {
					// TODO handle multiple data case
					cancelOldJobs();
					
					CupidDataCollector.record(
							CupidEventBuilder.contextEvent(getClass(), part, data, CupidActivator.getDefault())
							.create());
					
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
			
			SortedSet<ICapability> capabilities = CupidPlatform.getCapabilityRegistry().getCapabilities(TypeToken.of(argument.getClass()));

			for (ICapability capability : capabilities) {
				if (!hidden.contains(capability.getName())) {
					
					if (CapabilityUtil.isGenerator(capability)){
						rows.add(new CapabilityRow(capability, null));
						
					} else if (CapabilityUtil.isUnary(capability)
						&& TypeManager.isCompatible(CapabilityUtil.unaryParameter(capability), argument)){
						
						Object adapted = TypeManager.getCompatible(CapabilityUtil.unaryParameter(capability), argument);	
						rows.add(new CapabilityRow(capability, adapted));
					}
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
	
	private Row[] generateOutputRows(final CapabilityRow parent, final CapabilityStatus status){
		
		List<IOutput<?>> outputs = Lists.newArrayList(status.value().getOutputs().keySet());
		
		Collections.sort(outputs, new Comparator<IOutput<?>>() {
			@Override
			public int compare(IOutput<?> o1, IOutput<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		List<Row> result = Lists.newArrayList();
		
		for (IOutput<?> output : outputs){
			result.add(new OutputRow(parent, output, status.value().getOutput(output)));
		}
		
		return result.toArray(new Row[]{});
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
						if (!method.isAccessible()){
							method.setAccessible(true);
						}
						
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
		Display display = PlatformUI.getWorkbench().getDisplay();

		// does this need synchronization?
		if (!display.isDisposed()){
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!viewer.getTree().isDisposed()) {	
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
