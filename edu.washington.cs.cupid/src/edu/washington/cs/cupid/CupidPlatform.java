package edu.washington.cs.cupid;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;

import edu.washington.cs.cupid.capability.ICapabilityRegistry;
import edu.washington.cs.cupid.internal.CapabilityRegistry;
import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.preferences.PreferenceConstants;

/**
 * <p>The central class of the Cupid Platform Runtime. This class cannot be instantiated or subclassed by clients; 
 * all functionality is provided by static methods. Features include:</p>
 * 
 * <ul>
 *  <li>the platform registry of available capabilities</li>
 * </ul>
 * 
 * @author Todd Schiller (tws@cs.washington.edu)
 * @see {@link CapabilityExecutor} static methods for executing capabilities
 */
public final class CupidPlatform {

	private static final ICapabilityRegistry registry = new CapabilityRegistry();
	
	private CupidPlatform(){
		// NO OP	
	}
	
	public static File getPipelineDirectory(){
		IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
		return new File(preferences.getString(PreferenceConstants.P_ARROW_DIR));
	}
	
	public static ICapabilityRegistry getCapabilityRegistry(){
		return registry;
	}

}
