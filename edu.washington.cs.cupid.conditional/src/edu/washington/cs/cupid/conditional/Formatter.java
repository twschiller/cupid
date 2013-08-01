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

import java.util.Map;

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
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.decorators.DecoratorManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.conditional.internal.FormatUtil;
import edu.washington.cs.cupid.conditional.internal.FormatUtil.RuleCapabilityPair;
import edu.washington.cs.cupid.conditional.internal.NullPartListener;
import edu.washington.cs.cupid.conditional.internal.WorkbenchVisitor;
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
public class Formatter extends NullPartListener implements DisposeListener, FormattingRuleManager.RuleChangeListener {

	// TODO Later rules will override the formatting specified by earlier rule.
	// TODO Stale formatting needs to be cleared when the result is invalidated
	
	/**
	 * The original format for a workbench item; used to restore formats when recomputing capabilities.
	 * Items are removed from the map when they are disposed, or the conditional formatting is cleared.
	 */
	private final Map<Item, Format> originalItemFormats = Maps.newIdentityHashMap();
	
	private final Map<Item, Format> toReapply = Maps.newIdentityHashMap();
	
	/**
	 * Mapping from each item to the {@link Table} or {@link Tree} containing the 
	 * item. Items are removed from the map when they are disposed.
	 */
	private final Map<Item, Control> itemContainers = Maps.newIdentityHashMap();
	
	private final Object formatLock = new Object();
	
	private final IWorkbench workbench;
	private final FormattingRuleManager ruleManager;
	
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
	}
	
	/**
	 * <b>Must be run from the UI thread</b>
	 */
	public void formatActiveWindow(){
		formatVisitor.visit(workbench.getActiveWorkbenchWindow());
	}
	
	private void asyncConditionalFormat(final Control owner, final Item item){
		Object data = FormatUtil.data(item);
		
		for (RuleCapabilityPair rule : FormatUtil.rules(data)){
			final Object arg = TypeManager.getCompatible(CapabilityUtil.unaryParameter(rule.capability), data);
			asyncConditionalFormat(owner, item, rule.rule, rule.capability, arg);
		}
	}
		
	private void asyncConditionalFormat(final Control owner, final Item item, final FormattingRule rule, final ICapability capability, final Object input) {
		if (input == null) return;
		
		synchronized (formatLock) {
			itemContainers.put(item, owner);
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
						asyncFormat(owner, item, rule.getFormat());
					}
				}else{
//					System.out.println(
//							"Got error code for " + capability.getName() + ": " + 
//							status.getException().getMessage());
				}
			}
		});
	}
	
	private void asyncFormat(final Control owner, final Item item, final Format format){
		Preconditions.checkNotNull(format);
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (originalItemFormats) {
					if (!item.isDisposed()) {
						if (!originalItemFormats.containsKey(item)) {
							originalItemFormats.put(item, FormatUtil.getFormat(item));
						}
						FormatUtil.setFormat(owner, item, format);
						toReapply.put(item, format);
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
			System.out.println("Register for " + page.getLabel());
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
	
	/**
	 * Spawn a UI job to restore the original formatting for <tt>item</tt> and
	 * then re-run the formatting rules.
	 * @param item the tree or table item
	 */
	private void clearFormatting(final Item item){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (formatLock) {
					if (!item.isDisposed()){
						Control container = itemContainers.get(item);
						
						// Restore original Eclipse formatting
						asyncFormat(container, item, originalItemFormats.get(item));
						itemContainers.remove(item);
						
						// Reformat using current set of formatting rules
						asyncConditionalFormat(container, item);
					}
				}
			}
		});
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
			
			System.out.println("Page opened: " + page.getLabel());
			
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
	 * When an {@link Item} is disposed, remove it from the set of active items. If
	 * the item was the last item for an object, remove the object from the set of
	 * active objects.
	 * @param e the dispose event
	 */
	@Override
	public final void widgetDisposed(final DisposeEvent e) {
		if (e.getSource() instanceof Item) {
			Item item = (Item) e.getSource();
			
			synchronized (formatLock) {
				originalItemFormats.remove(item);
				itemContainers.remove(item);
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
			// XXX replace with faster code
			for (final Item item : Sets.newHashSet(itemContainers.keySet())){
				clearFormatting(item);
			}
		}
	}
	
	@Override
	public final void partActivated(final IWorkbenchPartReference partRef) {
		// NOP
	}

	@Override
	public final void partVisible(final IWorkbenchPartReference partRef) {
		// NOP
	}
	
	/**
	 * Whenever a decoration job completes, re-applies formatting.
	 * @author Todd Schiller
	 */
	private class DecorationManager extends NullJobListener{
		@Override
		public void done(IJobChangeEvent event) {
			if (event.getJob().belongsTo(DecoratorManager.FAMILY_DECORATE)){
				synchronized(formatLock){
					for (final Map.Entry<Item, Format> x : toReapply.entrySet()){
						Item item = (Item) x.getKey();
						asyncFormat(itemContainers.get(item), item, x.getValue());
					}
					toReapply.clear();
				}
			}
		}
	}
}
