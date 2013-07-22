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
package edu.washington.cs.cupid.conditional;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
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
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.IInvalidationListener;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IParameter;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.OutputSelector;
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.capability.snippet.SnippetCapability;
import edu.washington.cs.cupid.conditional.internal.Activator;
import edu.washington.cs.cupid.conditional.internal.NullPartListener;
import edu.washington.cs.cupid.jobs.ISchedulingRuleRegistry;
import edu.washington.cs.cupid.jobs.NullJobListener;

/**
 * Applies conditional formatting rules to workbench items.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class Formatter extends NullPartListener implements DisposeListener, IInvalidationListener {

	// TODO Later rules will override the formatting specified by earlier rule.
	// TODO Check if a rule was removed before applying it
	// TODO Stale formatting needs to be cleared when the result is invalidated
	
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
	
	private Map<FormattingRule, ICapability> capabilities = Maps.newIdentityHashMap();
	
	private Class<?> workbenchPartReferenceClazz = null;
	private Class<?> partPaneClazz = null;
	private Method getPaneMethod = null;
	private Method getControlMethod = null;
	
	private final ISchedulingRuleRegistry scheduler = CapabilityExecutor.getSchedulingRuleRegistry();
	
	private HashSet<FormattingRule> ruleErrors = Sets.newHashSet();
	
	/**
	 * Construct a listener that applies conditional formatting rules to workbench items.
	 * @throws Exception if instantiation fails
	 */
	public Formatter() throws Exception {
		workbenchPartReferenceClazz = Class.forName("org.eclipse.ui.internal.WorkbenchPartReference");
		partPaneClazz = Class.forName("org.eclipse.ui.internal.PartPane");
		getPaneMethod = workbenchPartReferenceClazz.getMethod("getPane");
		getControlMethod = partPaneClazz.getMethod("getControl");
		
		CapabilityExecutor.addCacheListener(this);
	}
	
	/**
	 * Returns the <i>first</i> data associated with <code>item</code>, or <code>null</code>.
	 * @param item the item
	 * @return the data associated with <code>item</code>
	 */
	private static Object data(final Item item) {
		Object object = item.getData();
		
		if (object == null) {
			return null;
		}
		
		if (object.getClass().isArray()) {
			Object result = null;
			for (Object element : (Object[]) object) {
				if (result == null) {
					result = element;
				} else {
					// TODO log warning properly
					break;
				}
			}
			return result;
		} else {
			return object;
		}
	}
		
	@Override
	public final void partActivated(final IWorkbenchPartReference partRef) {
		applyFormattingRules(partRef);
	}

	@Override
	public final void partVisible(final IWorkbenchPartReference partRef) {
		applyFormattingRules(partRef);
	}

	private static Format getFormat(final Widget object) {
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
	public static void applyFormat(final Widget object, final Format format) {
		checkArgument(!object.isDisposed(), "widget is disposed");
		
		Display display = Display.getDefault();
		Class<?> clazz = object.getClass();
		
		if (format.getBackground() != null) {
			try {
				clazz.getMethod("setBackground", Color.class).invoke(object, new Color(display, format.getBackground()));
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (format.getForeground() != null) {
			try {
				clazz.getMethod("setForeground", Color.class).invoke(object, new Color(display, format.getForeground()));
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (format.getFont() != null) {
			try {
				clazz.getMethod("setFont", Font.class).invoke(object, new Font(display, format.getFont()));
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	private void asyncFormat(final Item item, final FormattingRule rule, final ICapability capability, final Object input) {
		synchronized (activeObjects) {
			activeObjects.put(input, item);
			activeItems.put(item, input);
			item.addDisposeListener(this);
		}
	
		ICapabilityArguments packed = CapabilityUtil.packUnaryInput(capability, input);
		
		CapabilityExecutor.asyncExec(capability, packed, Formatter.this, new NullJobListener() {
			@Override
			public void done(final IJobChangeEvent event) {
				CapabilityJob<?> job = (CapabilityJob<?>) event.getJob();
				CapabilityStatus status = (CapabilityStatus) job.getResult();
			
				if (status.getCode() == Status.OK) {
					if ((Boolean) CapabilityUtil.singleOutputValue(capability, status)) {
						// apply the formatting rule
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								synchronized (originalFormats) {
									
									if (!item.isDisposed()) {
										if (!originalFormats.containsKey(item)) {
											originalFormats.put(item, getFormat(item));
										}
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
	
	private ICapability getCapabilityForRule(FormattingRule rule) throws ClassNotFoundException, NoSuchCapabilityException{
		if (capabilities.containsKey(rule)){
			return capabilities.get(rule);
		}
		
		TypeToken<?> inputType = TypeManager.forName(rule.getQualifiedType());
		
		ICapability c = rule.getCapabilityId() == null ? null : CupidPlatform.getCapabilityRegistry().findCapability(rule.getCapabilityId());
		ICapability.IOutput<?> o = (c == null) ? null : CapabilityUtil.findOutput(c, rule.getCapabilityOutput());
		
		TypeToken<?> snippetInputType = (o == null) ? inputType : o.getType();
				
		@SuppressWarnings({ "rawtypes", "unchecked" }) // checked when the snippet is written, and dynamically at runtime
		SnippetCapability s = rule.getSnippet() == null ? null :
			new SnippetCapability(
					rule.getName() + " snippet",
					"Predicate snippet for formatting rule " + rule.getName(),
					snippetInputType, TypeToken.of(boolean.class),
					rule.getSnippet());

		ICapability result;
		
		if (c != null && s != null){
			result = new DynamicSerializablePipeline(
					rule.getName() + " capability",
					"Capability for formatting rule " + rule.getName(),
					Lists.<Serializable>newArrayList(new OutputSelector(c, o), s),
					CapabilityUtil.noArgs(2));
		}else if (s != null){
			result = s;
		}else if (c != null){
			result = new OutputSelector(c, o);
		}else{
			throw new IllegalArgumentException("Formatting rule has no capability or predicate snippet");
		}
		
		capabilities.put(rule, result);
		return result;
	}
	
	
	/**
	 * Asynchronously apply formatting rules to <code>item</code> using {@link Activator#data(Item)} to 
	 * generate the input for the item.
	 * @param item the item
	 */
	private void applyFormattingRules(final Item item) {
		Object input = data(item);
		
		if (input == null) {
			return;
		}
		
		for (final FormattingRule rule : FormattingRuleManager.getInstance().activeRules()) {
			
			ICapability capability = null;

			try {
				capability = getCapabilityForRule(rule);
			} catch (Exception e) {
				if (!ruleErrors.contains(rule)){
					Activator.getDefault().logError("Error building capability for formatting rule: " + rule.getName(), e);
					ruleErrors.add(rule);
				}
				continue;
			}

			IParameter<?> parameter = CapabilityUtil.unaryParameter(capability);
			
			if (TypeManager.isCompatible(parameter, input)) {
				asyncFormat(item, rule, capability, TypeManager.getCompatible(parameter, input));
			}
		}
	}
	
	/**
	 * Recursively apply formatting rules to a TreeItem and its children.
	 * @param item the tree item
	 */
	private void applyFormattingRules(final TreeItem item) {
		applyFormattingRules((Item) item);
		for (TreeItem child : item.getItems()) {
			applyFormattingRules(child);
		}
	}
	
	/**
	 * Recursively apply {@link Activator#activeRules} to <code>control</code> and its children.
	 * @param control the control
	 */
	private void applyFormattingRules(final Control control) {
		if (!FormattingRuleManager.getInstance().activeRules().isEmpty()) {
			if (control instanceof Tree) {
				
				Tree tree = (Tree) control;
				
				tree.addTreeListener(new TreeListener() {
					@Override
					public void treeCollapsed(final TreeEvent e) {
						// NO OP
					}
					@Override
					public void treeExpanded(final TreeEvent e) {
						final TreeItem item = ((TreeItem) e.item);
						applyFormattingRules((TreeItem) item);
					}
				});
				
				for (final TreeItem item : tree.getItems()) {
					applyFormattingRules(item);
				}
			} else if (control instanceof Table) {
				for (final TableItem item : ((Table) control).getItems()) {
					applyFormattingRules(item);
				}
			}
			
			// recursively apply to children
			if (control instanceof Composite) {
				for (Control child : ((Composite) control).getChildren()) {
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
	public final void applyFormattingRules(final IWorkbenchPartReference partRef) {
		Control control = null;
		
		if (partRef instanceof IViewReference) {
			try {
				Object pane = getPaneMethod.invoke(partRef);
				control = (Control) getControlMethod.invoke(pane);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (control != null) {
				applyFormattingRules(control);
			}
		}
	}

	/**
	 * When an {@link Item} is disposed, remove it from the set of active items. If
	 * the item was the last item for an object, remove the object from the set of
	 * active objects.
	 * @param e the dispose event
	 */
	@Override
	public final void widgetDisposed(final DisposeEvent e) {
		if (e.getSource() instanceof Item) {
			Item item = (Item) e.getSource();
			
			synchronized (activeObjects) {
				Object input = activeItems.get(item);	
				activeObjects.remove(input, item);
				activeItems.remove(item);
			}
		}
	}
	
	/**
	 * Walks a resource delta to invalidate cache lines.
	 * @author Todd Schiller (tws@cs.washington.edu)
	 */
	private class InvalidationVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(final IResourceDelta delta) throws CoreException {
			if (delta.getAffectedChildren().length == 0) {
				IResource resource = delta.getResource();
				if (resource != null && interesting(delta)) {

					Set<Object> inputs = Sets.newHashSet(activeObjects.keySet());

					for (Object input : inputs) {
						
						if (resource.isConflicting(scheduler.getSchedulingRule(input))) {
							
							Collection<Item> items = activeObjects.get(input);
								
							for (final Item item : items) {
								if (!item.isDisposed()) {
									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											synchronized (originalFormats) {
												if (!item.isDisposed()) {
													if (originalFormats.containsKey(item)) {
														applyFormat(item, originalFormats.get(item));
													}
												}
											}
										}
									});
									
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
		private boolean interesting(final IResourceDelta delta) {
			return (delta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.TYPE)) != 0;
		}
	}

	@Override
	public final void onResourceChange(final Set<Object> invalidated, final IResourceChangeEvent event) {
		if (event.getDelta() != null) {
			synchronized (activeObjects) {
				try {
					event.getDelta().accept(new InvalidationVisitor());
				} catch (CoreException e) {
					throw new RuntimeException("Error invalidating conditional formats", e);
				}
			}
		}
	}
}
