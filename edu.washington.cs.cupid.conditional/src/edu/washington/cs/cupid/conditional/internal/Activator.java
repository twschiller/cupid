package edu.washington.cs.cupid.conditional.internal;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.MalformedCapabilityException;
import edu.washington.cs.cupid.capability.NoSuchCapabilityException;
import edu.washington.cs.cupid.conditional.Formatter;
import edu.washington.cs.cupid.conditional.FormattingRule;
import edu.washington.cs.cupid.conditional.preferences.PreferenceConstants;


/**
 * Activator for the conditional formatting plug-in
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class Activator extends AbstractUIPlugin implements IStartup{

	/** 
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "edu.washington.cs.cupid.conditional"; //$NON-NLS-1$

	/**
	 *  The shared instance
	 */
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * Applies conditional formatting rules to views when they are activated.
	 */
	private Formatter formatter;

	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		formatter = new Formatter();
		getPreferenceStore().addPropertyChangeListener(formatter);
		
		final IWorkbench workbench = PlatformUI.getWorkbench();
		
		workbench.getDisplay().asyncExec(new Runnable(){
			@Override
			public void run() {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				
				for (IWorkbenchPage page : window.getPages()){
					for (IViewReference view : page.getViewReferences()){
						formatter.applyFormattingRules(view);
					}
					
					page.addPartListener(formatter);
				}
				
				window.addPageListener(new IPageListener(){
					@Override
					public void pageActivated(IWorkbenchPage page) {
						page.addPartListener(formatter);
					}
					@Override
					public void pageClosed(IWorkbenchPage page) {
						page.removePartListener(formatter);
					}
					@Override
					public void pageOpened(IWorkbenchPage page) {
						page.addPartListener(formatter);
					}
				});
			}
		});
	}
	
	@Override
	public void earlyStartup() {
		// NO OP?	
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * @return the formatting rules in the preference store
	 */
	public FormattingRule[] storedRules(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String json = store.getString(PreferenceConstants.P_RULES);
		try{
			FormattingRule[] parsed = (new Gson()).fromJson(json, FormattingRule[].class);
			return parsed != null ? parsed : new FormattingRule[]{};
		}catch(Exception ex){
			throw new RuntimeException("Error loading formatting rules", ex);
		}
	}
	
	/**
	 * Returns the predicate capability for <code>rule</code>.
	 * @param rule the formatting rule.
	 * @return the predicate capability for <code>rule</code>
	 * @throws NoSuchCapabilityException iff the rule has no associated capability id, or no capability is available with the given id
	 * @throws MalformedCapabilityException iff the corresponding available capability is not a predicate 
     * (i.e., does not have a boolean return value)
	 */
	@SuppressWarnings("unchecked")
	public static ICapability<?, Boolean> findPredicate(FormattingRule rule) throws NoSuchCapabilityException, MalformedCapabilityException{
		Preconditions.checkNotNull(rule.getCapabilityId(), "Formatting rule must be associated with a capability");
		
		@SuppressWarnings("rawtypes")
		ICapability capability = CupidPlatform.getCapabilityRegistry().findCapability(rule.getCapabilityId());

		if (!TypeManager.isJavaCompatible(TypeToken.of(Boolean.class), capability.getReturnType())){
			throw new MalformedCapabilityException(capability, "Formatting rule requires predicate capability");
		}

		return capability;
	}

}
