package edu.washington.cs.cupid.conditional.internal;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.WeakHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.OutputSelector;
import edu.washington.cs.cupid.capability.ICapability.IParameter;
import edu.washington.cs.cupid.capability.dynamic.DynamicSerializablePipeline;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.capability.snippet.SnippetCapability;
import edu.washington.cs.cupid.conditional.Format;
import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.FormattingRuleManager;

/**
 * Utility methods for formatting tree and table items.
 * @author Todd Schiller
 */
public final class FormatUtil {

	private FormatUtil(){
		// NOP
	}
	
	/**
	 * Returns the <i>first</i> data associated with <code>item</code>, or <code>null</code>.
	 * @param item the item
	 * @return the data associated with <code>item</code>
	 */
	public static Object data(final Item item) {
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
	

	private static final HashSet<FormattingRule> ruleErrors = Sets.newHashSet();
	
	public static List<RuleCapabilityPair> rules(final Object data){
		List<RuleCapabilityPair> result = Lists.newArrayList();
	
		if (data == null){
			return result;
		}
		
		for (final FormattingRule rule : FormattingRuleManager.getInstance().activeRules()) {
			ICapability capability = null;

			try {
				capability = FormatUtil.getCapabilityForRule(rule);
			} catch (Exception e) {
				if (!ruleErrors.contains(rule)){
					Activator.getDefault().logError("Error building capability for formatting rule: " + rule.getName(), e);
					ruleErrors.add(rule);
				}
				continue;
			}

			IParameter<?> parameter = CapabilityUtil.unaryParameter(capability);
			if (TypeManager.isCompatible(parameter, data)) {
				result.add(new RuleCapabilityPair(rule, capability));
			}
		}
		
		return result;
	}
	
	private static final WeakHashMap<FormattingRule, ICapability> capabilityCache = new WeakHashMap<FormattingRule, ICapability>();
	
	public static ICapability getCapabilityForRule(FormattingRule rule) throws ClassNotFoundException, NoSuchCapabilityException{
		if (capabilityCache.containsKey(rule)){
			return capabilityCache.get(rule);
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
		
		capabilityCache.put(rule, result);
		return result;
	}
	
	/**
	 * Apply background color, foreground color, and font to <code>object</code>, if the object
	 * supports the corresponding setter methods.
	 * @param owner the parent table or tree
	 * @param widget the target
	 * @param format the format
	 */
	public static void setFormat(final Control owner, final Widget widget, final Format format) {
		checkArgument(!widget.isDisposed(), "widget is disposed");
		
		Display display = Display.getDefault();
		Class<?> clazz = widget.getClass();
		
		System.out.println("Applied format to object " + widget);
		
		widget.addListener(SWT.Paint | SWT.Paint, new Listener() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("Widget was repainted");
			}
		});
		
		if (format.getBackground() != null) {
			Color bg = new Color(display, format.getBackground());
			
			try {
				clazz.getMethod("setBackground", Color.class).invoke(widget, bg);
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			if (owner != null && owner instanceof Tree && widget instanceof TreeItem){
				for (int i = 0 ; i < ((Tree) owner).getColumnCount() + 1; i++){
					((TreeItem) widget).setBackground(i, bg);
				}
			}else if (owner != null && owner instanceof Table && widget instanceof TableItem){
				for (int i = 0 ; i < ((Table) owner).getColumnCount() + 1; i++){
					((TableItem) widget).setBackground(i, bg);
				}
			}
		}
		if (format.getForeground() != null) {
			Color fg = new Color(display, format.getForeground());
			
			try {
				clazz.getMethod("setForeground", Color.class).invoke(widget, fg);
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			if (owner != null && owner instanceof Tree && widget instanceof TreeItem){
				for (int i = 0 ; i < ((Tree) owner).getColumnCount() + 1; i++){
					((TreeItem) widget).setForeground(i, fg);
				}
			}else if (owner != null && owner instanceof Table && widget instanceof TableItem){
				for (int i = 0 ; i < ((Table) owner).getColumnCount() + 1; i++){
					((TableItem) widget).setForeground(i, fg);
				}
			}
		}
		if (format.getFont() != null) {
			Font font = new Font(display, format.getFont());
	
			try {
				clazz.getMethod("setFont", Font.class).invoke(widget, font);
			} catch (NoSuchMethodException e) {
				// NO OP
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			if (owner != null && owner instanceof Tree && widget instanceof TreeItem){
				for (int i = 0 ; i < ((Tree) owner).getColumnCount() + 1; i++){
					((TreeItem) widget).setFont(i, font);
				}
			}else if (owner != null && owner instanceof Table && widget instanceof TableItem){
				for (int i = 0 ; i < ((Table) owner).getColumnCount() + 1; i++){
					((TableItem) widget).setFont(i, font);
				}
			}
		}	
	}	
	
	/**
	 * Returns the formatting of <tt>widget</tt>. Does not return the individual cell coloring
	 * for items with multiple columns.
	 * @param widget the widget
	 * @return the formatting of <tt>widget</tt>
	 */
	public static Format getFormat(final Widget widget) {
		checkArgument(!widget.isDisposed(), "widget is disposed");
		
		Format result = new Format();
		
		Class<?> clazz = widget.getClass();
		
		try {
			result.setBackground(((Color) clazz.getMethod("getBackground").invoke(widget)).getRGB());
		} catch (NoSuchMethodException e) {
			// NO OP
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			result.setForeground(((Color) clazz.getMethod("getForeground").invoke(widget)).getRGB());
		} catch (NoSuchMethodException e) {
			// NO OP
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			result.setFont(((Font) clazz.getMethod("getFont").invoke(widget)).getFontData());
		} catch (NoSuchMethodException e) {
			// NO OP
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}

	public static class RuleCapabilityPair{
		public final FormattingRule rule;
		public final ICapability capability;
		
		public RuleCapabilityPair(FormattingRule rule, ICapability capability) {
			this.rule = rule;
			this.capability = capability;
		}
	}
	
}
