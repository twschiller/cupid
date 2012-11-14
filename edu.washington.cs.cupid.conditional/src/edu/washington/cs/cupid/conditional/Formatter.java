package edu.washington.cs.cupid.conditional;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.IInvalidationListener;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.conditional.internal.Activator;
import edu.washington.cs.cupid.conditional.internal.NullPartListener;
import edu.washington.cs.cupid.conditional.preferences.PreferenceConstants;
import edu.washington.cs.cupid.jobs.NullJobListener;

/**
 * Applies conditional formatting rules to workbench items.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class Formatter extends NullPartListener implements IPropertyChangeListener, DisposeListener, IInvalidationListener{

	// TODO Later rules will override the formatting specified by earlier rule.
	// TODO Check if a rule was removed before applying it
	// TODO Stale formatting needs to be cleared when the result is invalidated
	
	/**
	 * Active rules, ordered by precedence.
	 */
	private List<FormattingRule> activeRules = Lists.newArrayList();

	/**
	 * The original format for a workbench item; used to restore formats when recomputing capabilities.
	 */
	private Map<Item, Format> originalFormats = Maps.newIdentityHashMap();
	
	/**
	 * The set of inputs and their associated display items.
	 */
	private Multimap<Object, Item> activeObjects = HashMultimap.create();
	
	/**
	 * The set of items with conditional formatting, and their associated inputs. Guarded by monitor lock for {@link Formatter#activeObjects}. 
	 * XXX what if the associated input changes?
	 */
	private IdentityHashMap<Item, Object> activeItems = Maps.newIdentityHashMap();
	
	private Class<?> workbenchPartReferenceClazz = null;
	private Class<?> partPaneClazz = null;
	private Method getPaneMethod = null;
	private Method getControlMethod = null;
	
	public Formatter() throws ClassNotFoundException, SecurityException, NoSuchMethodException{
		workbenchPartReferenceClazz = Class.forName("org.eclipse.ui.internal.WorkbenchPartReference");
		partPaneClazz = Class.forName("org.eclipse.ui.internal.PartPane");
		getPaneMethod = workbenchPartReferenceClazz.getMethod("getPane");
		getControlMethod = partPaneClazz.getMethod("getControl");
		updateRules();
		
		CapabilityExecutor.addCacheListener(this);
	}
	
	/**
	 * Returns the <i>first</i> data associated with <code>item</code>, or <code>null</code>.
	 * @param item the item
	 * @return the data associated with <code>item</code>
	 */
	private static Object data(Item item){
		Object object = item.getData();
		
		if (object == null){
			return null;
		}
		
		if (object.getClass().isArray()){
			Object result = null;
			for (Object element : (Object[]) object){
				if (result == null){
					result = element;
				}else{
					System.out.println("WARNING: item " + item + " has multiple data");
					break;
				}
			}
			return result;
		}else{
			return object;
		}
	}
	
	/**
	 * Set {@link Activator#activeRules} to the list of rules that are active
	 * and have an associated capability.
	 */
	private void updateRules(){
		synchronized(activeRules){
			activeRules.clear();
			for (FormattingRule rule : Activator.getDefault().storedRules()){
				if (rule.isActive() && rule.getCapabilityId() != null){
					activeRules.add(rule);
				}
			}
		}
	}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		System.out.println("Part Activated: " + partRef.getPartName());
		applyFormattingRules(partRef);
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		System.out.println("Part Visible: " + partRef.getPartName());
		applyFormattingRules(partRef);
	}

	private static Format getFormat(Widget object){
		checkArgument(!object.isDisposed(), "widget is disposed");
		
		Format result = new Format();
		
		Class<?> clazz = object.getClass();
		
		try {
			result.setBackground(((Color) clazz.getMethod("getBackground").invoke(object)).getRGB());
		} catch (NoSuchMethodException e) {
			// NO OP
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			result.setForeground(((Color) clazz.getMethod("getForeground").invoke(object)).getRGB());
		} catch (NoSuchMethodException e) {
			// NO OP
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			result.setFont(((Font) clazz.getMethod("getFont").invoke(object)).getFontData());
		} catch (NoSuchMethodException e) {
			// NO OP
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}

	/**
	 * Apply background color, foreground color, and font to <code>object</code>, if the object
	 * supports the corresponding setter methods.
	 * @param object the target
	 * @param format the format
	 */
	public static void applyFormat(Widget object, Format format){
		checkArgument(!object.isDisposed(), "widget is disposed");
		
		Display display = Display.getDefault();
		Class<?> clazz = object.getClass();
		
		if (format.getBackground() != null){
			try {
				clazz.getMethod("setBackground", Color.class).invoke(object, new Color(display, format.getBackground()));
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (format.getForeground() != null){
			try {
				clazz.getMethod("setForeground", Color.class).invoke(object, new Color(display, format.getForeground()));
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (format.getFont() != null){
			try {
				clazz.getMethod("setFont", Font.class).invoke(object, new Font(display, format.getFont()));
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void asyncFormat(final Item item, final FormattingRule rule, @SuppressWarnings("rawtypes") ICapability capability, Object input){
		synchronized(activeObjects){
			activeObjects.put(input, item);
			activeItems.put(item, input);
			item.addDisposeListener(this);
		}
		
		CapabilityExecutor.asyncExec(capability, input, Formatter.this, new NullJobListener(){
			@SuppressWarnings("rawtypes")
			@Override
			public void done(IJobChangeEvent event) {
				CapabilityJob job = (CapabilityJob) event.getJob();
				CapabilityStatus<Boolean> status = (CapabilityStatus<Boolean>) job.getResult();
			
				if (status.getCode() == Status.OK ){
					if (status.value()){
						// apply the formatting rule
						Display.getDefault().asyncExec(new Runnable(){
							@Override
							public void run() {
								synchronized(originalFormats){
									
									if (!item.isDisposed()){
										if (!originalFormats.containsKey(item)){
											System.out.println("Saving original format " + item.getText() + " (" + item.hashCode() + ")");
											originalFormats.put(item, getFormat(item));
										}
										System.out.println("Formatting " + item.getText() + " (" + item.hashCode() + ") with rule " + rule.getName());
										applyFormat(item, rule.getFormat());
									}

								}
							}
						});
					}
				}
			}
		});
	}
	
	/**
	 * Asynchronously apply formatting rules to <code>item</code> using {@link Activator#data(Item)} to 
	 * generate the input for the item.
	 * @param item the item
	 */
	private void applyFormattingRules(final Item item){
		Object input = data(item);
		
		if (input == null){
			System.out.println("No input associated with " + item + "; text: " + item.getText());
			return;
		}else{
			System.out.println("Applying formatting rules to " + item);
		}
		
		int matches = 0;
		
		for (final FormattingRule rule : activeRules){
			
			@SuppressWarnings("rawtypes")
			ICapability capability = null;

			try {
				capability = Activator.findPredicate(rule);
			} catch (Exception e) {
				continue;
				// TODO error information needs to be aggregated and passed up the line
			}

			if (CapabilityExecutor.isCompatible(capability, input)){
				matches++;
				asyncFormat(item, rule, capability, input);
			}
		}
		
		System.out.println(input.getClass().getSimpleName() + " has " + matches + " predicates");
	}
	
	/**
	 * Recursively apply formatting rules to a TreeItem and its children
	 * @param item the tree item
	 */
	private void applyFormattingRules(TreeItem item){
		applyFormattingRules((Item) item);
		for (TreeItem child : item.getItems()){
			applyFormattingRules(child);
		}
	}
	
	/**
	 * Recursively apply {@link Activator#activeRules} to <code>control</code> and its children.
	 * @param control the control
	 */
	private void applyFormattingRules(final Control control){
		if (!activeRules.isEmpty()){
			if (control instanceof Tree){
				
				Tree tree = (Tree) control;
				
				tree.addTreeListener(new TreeListener(){
					@Override
					public void treeCollapsed(TreeEvent e) {
						// NO OP
					}
					@Override
					public void treeExpanded(TreeEvent e) {
						final TreeItem item = ((TreeItem) e.item);
						System.out.println("Expanding " + item);
						applyFormattingRules((TreeItem) item);
					}
				});
				
				for (final TreeItem item : tree.getItems()){
					applyFormattingRules(item);
				}
			}else if (control instanceof Table){
				for (final TableItem item : ((Table) control).getItems()){
					applyFormattingRules(item);
				}
			}
			
			// recursively apply to children
			if (control instanceof Composite){
				for (Control child : ((Composite) control).getChildren()){
					applyFormattingRules(child);
				}
			}
		}
	}
	
	/**
	 * If <code>partRef</code> is a {@link IViewReference}, applys the active formatting rules
	 * to the controls in the view.
	 * @param partRef the workbench part
	 */
	public void applyFormattingRules(IWorkbenchPartReference partRef){
		Control control = null;
		
		if (partRef instanceof IViewReference){
			try {
				Object pane = getPaneMethod.invoke(partRef);
				control = (Control) getControlMethod.invoke(pane);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (control != null){
				applyFormattingRules(control);
			}
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_RULES)){
			updateRules();
		}
	}

	/**
	 * When an {@link Item} is disposed, remove it from the set of active items. If
	 * the item was the last item for an object, remove the object from the set of
	 * active objects.
	 */
	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (e.getSource() instanceof Item){
			Item item = (Item) e.getSource();
			
			System.out.println("Disposing " + item.getText() + " (" + item.hashCode() + ")");
			
			synchronized(activeObjects){
				Object input = activeItems.get(item);	
				activeObjects.remove(input, item);
				activeItems.remove(item);
			}
		}
	}
	
	/**
	 * Walks a resource delta to invalidate cache lines
	 * @author Todd Schiller (tws@cs.washington.edu)
	 */
	private class InvalidationVisitor implements IResourceDeltaVisitor{
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (delta.getAffectedChildren().length == 0){
				IResource resource = delta.getResource();
				if (resource != null && interesting(delta)){

					Set<Object> inputs = Sets.newHashSet(activeObjects.keySet());

					for (Object input : inputs){
						if (resource.isConflicting(CapabilityExecutor.schedulingRule(input))){
							
							Collection<Item> items = activeObjects.get(input);
							
							System.out.println("Conflicting items: " + items.toString());
							
							for (final Item item : items){
								if (!item.isDisposed()){
									Display.getDefault().asyncExec(new Runnable(){
										@Override
										public void run() {
											synchronized(originalFormats){
												if (!item.isDisposed()){
													if (originalFormats.containsKey(item)){
														System.out.println("Resetting " + item.getText() + " (" + item.hashCode() + ")");
														applyFormat(item, originalFormats.get(item));
													}
												}
											}
										}
									});
									
									System.out.println("Reformatting " + item.getText() + " (" + item.hashCode() + ")");
									applyFormattingRules(item);
								}
								
							}
						}
					}

				}
			}
			return true;
		}
		
		/**
		 * @param delta the delta
		 * @return <code>true</code> iff the delta causes resources to be invalidated
		 */
		private boolean interesting(IResourceDelta delta){
			return (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.TYPE)) != 0;
		}
	}

	@Override
	public void onResourceChange(Set<Object> invalidated, IResourceChangeEvent event) {
		if (event.getDelta() != null){
			synchronized(activeObjects){
				try {
					event.getDelta().accept(new InvalidationVisitor());
				} catch (CoreException e) {
					throw new RuntimeException("Error invalidating conditional formats", e);
				}
			}
		}
	}
}
