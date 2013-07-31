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

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.IInvalidationListener;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.conditional.internal.FormatUtil;
import edu.washington.cs.cupid.conditional.internal.FormatUtil.RuleCapabilityPair;
import edu.washington.cs.cupid.conditional.internal.NullPartListener;
import edu.washington.cs.cupid.conditional.internal.WorkbenchVisitor;
import edu.washington.cs.cupid.jobs.ISchedulingRuleRegistry;
import edu.washington.cs.cupid.jobs.NullJobListener;

/**
 * Applies conditional formatting rules to workbench items. Formatting rules are applied at the following times:
 * <ul>
 *  <li> When the formatting rule is first created / initialized </li>
 *  <li> When item becomes viewable (e.g., the parent tree node is expanded) </li>
 *  <li> When the item is decorated, since some decorations revert the formatting </li>
 * </ul>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @see {@link FormatUtil} Utility methods for performing formatting
 */
public class Formatter extends NullPartListener implements DisposeListener, IInvalidationListener, FormattingRuleManager.RuleChangeListener {

	// TODO Later rules will override the formatting specified by earlier rule.
	// TODO Check if a rule was removed before applying it
	// TODO Stale formatting needs to be cleared when the result is invalidated
	
	/**
	 * The original format for a workbench item; used to restore formats when recomputing capabilities.
	 */
	private final Map<Item, Format> originalFormats = Maps.newIdentityHashMap();
	
	/**
	 * The set of inputs and their associated display items.
	 */
	private final Multimap<Object, Item> activeObjects = HashMultimap.create();
	
	/**
	 * The set of items with conditional formatting, and their associated inputs. Guarded by monitor lock for {@link Formatter#activeObjects}. 
	 * XXX what if the associated input changes?
	 */
	private final IdentityHashMap<Item, Object> activeItems = Maps.newIdentityHashMap();
	
	private final WeakHashMap<Item, Format> activeFormats = new WeakHashMap<Item, Format>();
	
	private final ISchedulingRuleRegistry scheduler = CapabilityExecutor.getSchedulingRuleRegistry();
	
	private final FormattingRuleManager ruleManager;
	
	private final IWorkbench workbench;
	
	private final WorkbenchFormatter formatVisitor;
	private final WorkbenchRegister registerVisitor;
	
	private final PageListener pageListener = new PageListener();
	
	/**
	 * Construct a listener that applies conditional formatting rules to workbench items.
	 * @throws Exception if instantiation fails
	 */
	public Formatter(IWorkbench workbench, FormattingRuleManager ruleManager) throws Exception {
		this.workbench = workbench;
		this.ruleManager = ruleManager;
		this.formatVisitor = new WorkbenchFormatter();
		this.registerVisitor = new WorkbenchRegister();
		
		CapabilityExecutor.addCacheListener(this);
		this.ruleManager.addRuleChangeListener(this);
		
		final IDecoratorManager decorationManager = workbench.getDecoratorManager();
		decorationManager.addListener(new ReapplyFormatListener());
		
		registerVisitor.visit(workbench);
	}
	
	public void formatActiveWindow(){
		formatVisitor.visit(workbench.getActiveWorkbenchWindow());
	}
	
	private void asyncConditionalFormat(final Control owner, final Item item){
		Object input = FormatUtil.data(item);
		
		for (RuleCapabilityPair rule : FormatUtil.rules(input)){
			asyncConditionalFormat(owner, item, rule.rule, rule.capability, input);
		}
	}
		
	private void asyncConditionalFormat(final Control owner, final Item item, final FormattingRule rule, final ICapability capability, final Object input) {
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
						asyncFormat(owner, item, rule);
					}
				}
			}
		});
	}
	
	private void asyncFormat(final Control owner, final Item item, final FormattingRule rule){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (originalFormats) {
					if (!item.isDisposed()) {
						if (!originalFormats.containsKey(item)) {
							originalFormats.put(item, FormatUtil.getFormat(item));
						}
						FormatUtil.setFormat(owner, item, rule.getFormat());
					}
				}
			}
		});
	}
	
	private class WorkbenchRegister extends WorkbenchVisitor{
		public WorkbenchRegister() throws Exception {
			super();
		}
		@Override
		public boolean visit(Tree tree) {
			tree.addTreeListener(new TreeExpandFormatter(tree));
			return true;
		}
		@Override
		public boolean visit(IWorkbenchWindow window) {
			window.addPageListener(pageListener);
			return true;
		}
		@Override
		public boolean visit(IWorkbenchPage page) {
			page.addPartListener(Formatter.this);
			return true;
		}
	}
	
	private class WorkbenchFormatter extends WorkbenchVisitor{
		public WorkbenchFormatter() throws Exception {
			super();
		}

		@Override
		public boolean visit(Tree tree, TreeItem item) {
			asyncConditionalFormat(tree, item);
			return true;
		}

		@Override
		public boolean visit(Table table, TableItem item) {
			asyncConditionalFormat(table, item);
			return true;
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
// TODO: track owner?
//							for (final Item item : items) {
//								if (!item.isDisposed()) {
//									Display.getDefault().asyncExec(new Runnable() {
//										@Override
//										public void run() {
//											synchronized (originalFormats) {
//												if (!item.isDisposed()) {
//													if (originalFormats.containsKey(item)) {
//														applyFormat(item, originalFormats.get(item));
//													}
//												}
//											}
//										}
//									});
//									
//									applyFormattingRules(item);
//								}
//								
//							}
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

	private class PageListener implements IPageListener{
		@Override
		public void pageActivated(IWorkbenchPage page) {
			page.addPartListener(Formatter.this);
		}

		@Override
		public void pageClosed(IWorkbenchPage page) {
			page.addPartListener(Formatter.this);
		}

		@Override
		public void pageOpened(IWorkbenchPage page) {
			page.addPartListener(Formatter.this);
		}
	}
	
	private final class TreeExpandFormatter implements TreeListener{
		private final Tree tree;
		
		private TreeExpandFormatter(Tree tree) {
			this.tree = tree;
		}

		@Override
		public void treeCollapsed(TreeEvent e) {
			// NOP
		}

		@Override
		public void treeExpanded(TreeEvent e) {
			formatVisitor.visit(tree, (TreeItem) e.item);
		}
	}
	
	private class ReapplyFormatListener implements ILabelProviderListener{
		@Override
		public void labelProviderChanged(LabelProviderChangedEvent event) {
			if (event.getElement() != null){
				Format f = activeFormats.get(event.getElement());
				if (f != null){
					// XXX
				}
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
	
	@Override
	public void ruleActivated(FormattingRule rule) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ruleDeactivated(FormattingRule rule) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public final void partActivated(final IWorkbenchPartReference partRef) {
		formatVisitor.visit(partRef);
	}

	@Override
	public final void partVisible(final IWorkbenchPartReference partRef) {
		formatVisitor.visit(partRef);
	}
}
