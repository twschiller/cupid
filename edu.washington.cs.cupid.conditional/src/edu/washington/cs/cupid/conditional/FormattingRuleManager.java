package edu.washington.cs.cupid.conditional;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.exception.NoSuchCapabilityException;
import edu.washington.cs.cupid.conditional.internal.Activator;
import edu.washington.cs.cupid.conditional.preferences.PreferenceConstants;
import edu.washington.cs.cupid.usage.CupidDataCollector;
import edu.washington.cs.cupid.usage.events.CupidEventBuilder;

public class FormattingRuleManager implements IPropertyChangeListener {

	private static FormattingRuleManager instance = new FormattingRuleManager();
	
	private FormattingRuleManager(){
		updateRules();
	}
	
	public static FormattingRuleManager getInstance(){
		return instance;
	}
	
	/**
	 * Active rules, ordered by precedence.
	 */
	private List<FormattingRule> activeRules = Lists.newArrayList();
	
	/**
	 * @return the formatting rules in the preference store
	 */
	public FormattingRule[] storedRules() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String json = store.getString(PreferenceConstants.P_RULES);
		try {
			FormattingRule[] parsed = (new Gson()).fromJson(json, FormattingRule[].class);
			return parsed != null ? parsed : new FormattingRule[]{};
		} catch (Exception ex) {
			throw new RuntimeException("Error loading formatting rules", ex);
		}
	}
	
	public List<FormattingRule> activeRules() {
		synchronized(activeRules){
			return Lists.newArrayList(activeRules);
		}
	}
	
	/**
	 * Set {@link Activator#activeRules} to the list of rules that are active
	 * and have an associated capability.
	 */
	private void updateRules() {
		synchronized (activeRules) {
			List<FormattingRule> current = Lists.newArrayList();
			
			for (FormattingRule rule : storedRules()) {
				if (rule.isActive() && rule.getCapabilityId() != null) {
					current.add(rule);
				}
			}

			Set<FormattingRule> newSet = Sets.newHashSet(current);
			Set<FormattingRule> oldSet = Sets.newHashSet(activeRules);
			
			for (FormattingRule rule : Sets.difference(newSet, oldSet)) {
				CupidDataCollector.record(createRuleEvent("enableFormattingRule", rule).create());
			}
			
			for (FormattingRule rule : Sets.difference(oldSet, newSet)) {
				CupidDataCollector.record(createRuleEvent("disableFormattingRule", rule).create());
			}
			
			activeRules.clear();
			activeRules.addAll(current);
		}
	}
	
	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_RULES)) {
			updateRules();
		}
	}
	
	private CupidEventBuilder createRuleEvent(String what, FormattingRule rule){
		CupidEventBuilder event = 
				new CupidEventBuilder(what, getClass(), Activator.getDefault())
					.addData("name", rule.getName())
					.addData("capabilityId", rule.getCapabilityId());
		
		try {
			ICapability capability = CupidPlatform.getCapabilityRegistry().findCapability(rule.getCapabilityId());
			event.addData("capabilityName", capability.getName());
			event.addData("parameterType", CapabilityUtil.unaryParameter(capability).getType().toString());
		} catch (NoSuchCapabilityException e) {
			// NO OP
		}
		
		return event;
	}

	public void addTemporaryRule(FormattingRule rule){
		synchronized (activeRules) {
			activeRules.add(rule);
		}
	}
	
	public void addRule(FormattingRule rule){
		synchronized (activeRules) {
			FormattingRule rOld[] = storedRules();
			
			Gson gson = new Gson();
			FormattingRule rNew[] = new FormattingRule[rOld.length + 1];
			System.arraycopy(rOld, 0, rNew, 0, rOld.length);
			rNew[rOld.length] = rule;
		
			Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_RULES, gson.toJson(rNew));
		}
	}	
}
