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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.decorators.DecoratorManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.IInvalidationListener;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
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
	
	/**
	 * The original format for a workbench item; used to restore formats when recomputing capabilities.
	 * Items are removed from the map when they are disposed, or the conditional formatting is cleared.
	 */
	private final Map<Item, Format> originalFormats = Maps.newIdentityHashMap();
	
	/**
	 * The format applied to an item as the result of conditional formatting. When a decoration
	 * event occurs, these formats are immediately re-applied, and the conditional formatting
	 * job is kicked of to update conditional formats. Items are removed from the map when they
	 * are disposed, or if no conditional formatting rule applies.
	 */
	private final Map<Item, Format> conditionalFormats = Maps.newIdentityHashMap();
		
	/**
	 * Mapping from each item to the {@link Table} or {@link Tree} containing the 
	 * item. Items are removed from the map when they are disposed.
	 */
	private final Map<Item, Control> itemContainers = Maps.newIdentityHashMap();
	
	private final Set<Item> pending = Sets.newIdentityHashSet();
	
	private final WeakHashMap<Object, Set<Item>> viewData = new WeakHashMap<Object, Set<Item>>();
	
	private final Object formatLock = new Object();
	
	private final IWorkbench workbench;
	private final FormattingRuleManager ruleManager;
	
	private final ISchedulingRuleRegistry scheduler = CapabilityExecutor.getSchedulingRuleRegistry();
	
	/**
	 * Construct a listener that applies conditional formatting rules to workbench items.
	 * @throws Exception if instantiation fails
	 */
	public Formatter(IWorkbench workbench, FormattingRuleManager ruleManager) throws Exception {
		this.workbench = workbench;
		this.ruleManager = ruleManager;
		this.formatVisitor = new WorkbenchFormatter();
		this.registerVisitor = new WorkbenchRegister();
		this.ruleManager.addRuleChangeListener(this);
	}
	
	/**
	 * Registers the formatter with the workbench.
	 * <b>Must be run from the UI thread</b>
	 */
	public void initialize(){
		registerVisitor.visit(workbench);
		Job.getJobManager().addJobChangeListener(new DecorationManager());
		CapabilityExecutor.addCacheListener(this);
	}
	
	/**
	 * <b>Must be run from the UI thread.</b>
	 */
	public void formatActiveWindow(){
		formatVisitor.visit(workbench.getActiveWorkbenchWindow());
	}
	
	/**
	 * Apply conditional formatting to the item, or clear any formatting if no rules
	 * apply. <b>Must be run from the UI thread.</b>
	 * @param owner the table / tree
	 * @param item the item to conditionally format
	 */
	private void asyncConditionalFormat(final Control owner, final Item item){
		Display.getDefault().asyncExec(new Runnable() {	
			@Override
			public void run() {
				if (!item.isDisposed()){
					Object data = FormatUtil.data(item);

					Queue<RuleCapabilityPair> ruleQueue = Lists.newLinkedList(FormatUtil.rules(data));
					if (!ruleQueue.isEmpty()){
						// Apply new formatting
						asyncConditionalFormat(owner, item, ruleQueue, data);
					}else if (originalFormats.containsKey(item)){
						// Restore original formatting
						FormatUtil.setFormat(owner, item, originalFormats.get(item));
					}else{
						FormatUtil.setFormat(owner, item, new Format());
					}
				}
			}
		});
	}
	
	private void asyncConditionalFormat(final Control owner, final Item item, final Queue<RuleCapabilityPair> ruleQueue, final Object data) {
		if (data == null) return;
		
		synchronized (formatLock) {
			if (pending.contains(item)){
				return;
			}
			
			itemContainers.put(item, owner);
			item.addDisposeListener(this);
			
			if (!viewData.containsKey(data)){
				viewData.put(data, Sets.<Item>newIdentityHashSet());
			}
			viewData.get(data).add(item);
			pending.add(item);
		}
		
		new SerialRuleExecutor(owner, item, ruleQueue, data).run();
	}
	
	private class SerialRuleExecutor extends NullJobListener{

		private final List<Format> resultQueue = Lists.newLinkedList();
		
		private final Control owner;
		private final Item item;
		private final Queue<RuleCapabilityPair> ruleQueue;
		private final Object data;
		
		private RuleCapabilityPair current = null;
		
		public SerialRuleExecutor(Control owner, Item item, Queue<RuleCapabilityPair> ruleQueue, Object data) {
			this.owner = owner;
			this.item = item;
			this.data = data;
			this.ruleQueue = ruleQueue;
		}	
		
		private void run(){
			if (ruleQueue.isEmpty()) return;
			
			current = ruleQueue.poll();	
			final Object arg = TypeManager.getCompatible(CapabilityUtil.unaryParameter(current.capability), data);
			final ICapabilityArguments packed = CapabilityUtil.packUnaryInput(current.capability, arg);
			CapabilityExecutor.asyncExec(current.capability, packed, Formatter.this, this);
		}
		
		@Override
		public void done(IJobChangeEvent event) {
			CapabilityJob<?> job = (CapabilityJob<?>) event.getJob();
			CapabilityStatus status = (CapabilityStatus) job.getResult();
		
			if (status.getCode() == Status.OK) {
				if ((Boolean) CapabilityUtil.singleOutputValue(current.capability, status)) {
					resultQueue.add(current.rule.getFormat());
				}
			} else {
				status.getException().printStackTrace();
			}
			
			if (!ruleQueue.isEmpty()){
				// need to keep calculating capabilities
				run();
			}else if (!resultQueue.isEmpty()){
				// apply the new formatting
				if (originalFormats.containsKey(item)){
					resultQueue.add(0, originalFormats.get(item));
				}
				asyncFormat(owner, item, FormatUtil.merge(resultQueue));
			}else {
				// clear the formatting
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (formatLock) {
							if (!item.isDisposed() && originalFormats.containsKey(item)) {
								FormatUtil.setFormat(owner, item, originalFormats.get(item));
							}
							pending.remove(item);
							conditionalFormats.remove(item);
						}
					}
				});			
			}
		}
	}
	
	private void asyncFormat(final Control owner, final Item item, final Format format){
		Preconditions.checkNotNull(format);
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (formatLock) {
					if (!item.isDisposed()) {
						if (!originalFormats.containsKey(item)){
							originalFormats.put(item, FormatUtil.getFormat(item));
						}
						
						FormatUtil.setFormat(owner, item, format);
						conditionalFormats.put(item, format);
						pending.remove(item);
					}
				}
			}
		});
	}
	
	private final WorkbenchRegister registerVisitor;
	
	private class WorkbenchRegister extends WorkbenchVisitor{
		public WorkbenchRegister() throws Exception {
			super();
		}
		@Override
		public void visit(Tree tree) {
			tree.addTreeListener(new TreeExpandFormatter(tree));
			super.visit(tree);
		}
		
		@Override
		public void visit(IWorkbenchWindow window) {
			window.addPageListener(pageListener);
			super.visit(window);
		}
		@Override
		public void visit(IWorkbenchPage page) {
			page.addPartListener(Formatter.this);
			super.visit(page);
		}
	}
	
	private final WorkbenchFormatter formatVisitor;
	
	private class WorkbenchFormatter extends WorkbenchVisitor{
		public WorkbenchFormatter() throws Exception {
			super();
		}

		@Override
		public void visit(Tree tree, TreeItem item) {
			asyncConditionalFormat(tree, item);
			super.visit(tree, item);
		}

		@Override
		public void visit(Table table, TableItem item) {
			asyncConditionalFormat(table, item);
			super.visit(table, item);
		}
	}
	
	private final PageListener pageListener = new PageListener();

	private class PageListener implements IPageListener{
		@Override
		public void pageActivated(final IWorkbenchPage page) {
			page.addPartListener(Formatter.this);
		}

		@Override
		public void pageClosed(final IWorkbenchPage page) {
			// NOP
		}

		@Override
		public void pageOpened(final IWorkbenchPage page) {
			page.addPartListener(Formatter.this);
			
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					registerVisitor.visit(page);
					formatVisitor.visit(page);
				}
			});
		}
	}
	
	private final class TreeExpandFormatter implements TreeListener{
		private final Tree tree;
		
		private TreeExpandFormatter(final Tree tree) {
			this.tree = tree;
		}

		@Override
		public void treeCollapsed(final TreeEvent e) {
			// NOP
		}

		@Override
		public void treeExpanded(final TreeEvent e) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					formatVisitor.visit(tree, (TreeItem) e.item);
				}
			});
		}
	}
		
	/**
	 * When an {@link Item} is disposed, remove all formatting information.
	 * @param e the dispose event
	 */
	@Override
	public final void widgetDisposed(final DisposeEvent e) {
		if (e.getSource() instanceof Item) {
			Item item = (Item) e.getSource();
			
			synchronized (formatLock) {
				originalFormats.remove(item);
				conditionalFormats.remove(item);
				itemContainers.remove(item);
				
				for (Set<Item> viewItems : viewData.values()){
					viewItems.remove(item);
				}
			}
		}
	}
	
	@Override
	public void ruleActivated(final FormattingRule rule) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				formatVisitor.visit(workbench);
			}
		});
	}

	@Override
	public void ruleDeactivated(final FormattingRule rule) {
		synchronized (formatLock){
			// Re-apply formatting rules to all formatted items
			for (final Item item : Sets.newHashSet(conditionalFormats.keySet())){
				asyncConditionalFormat(itemContainers.get(item), item);
			}
		}
	}
	
	@Override
	public final void onResourceChange(final Set<Object> invalidated, final IResourceChangeEvent event) {
		if (event.getDelta() != null) {
			synchronized (formatLock) {
				try {
					event.getDelta().accept(new InvalidationVisitor());
				} catch (CoreException e) {
					throw new RuntimeException("Error invalidating conditional formats", e);
				}
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
					Set<Object> inputs = Sets.newHashSet(viewData.keySet());
					for (Object input : inputs) {
						if (resource.isConflicting(scheduler.getSchedulingRule(input))) {		
							for (final Item item : viewData.get(input)){
								asyncConditionalFormat(itemContainers.get(item), item);
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
		
	/**
	 * Whenever a decoration job completes, re-applies the current conditional formatting
	 * and then kicks of a new conditional formatting job.
	 * @author Todd Schiller
	 */
	private class DecorationManager extends NullJobListener{
		@Override
		public void done(IJobChangeEvent event) {
			if (event.getJob().belongsTo(DecoratorManager.FAMILY_DECORATE)){
				
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized(formatLock){
							for (final Map.Entry<Item, Format> x : conditionalFormats.entrySet()){
								Item item = (Item) x.getKey();			
								
								if (!pending.contains(item)){
									Control container = itemContainers.get(item);
									FormatUtil.setFormat(container, item, x.getValue());
									asyncConditionalFormat(container, item);		
								}
							}
						}
					}
				});
			}
		}
	}
}
