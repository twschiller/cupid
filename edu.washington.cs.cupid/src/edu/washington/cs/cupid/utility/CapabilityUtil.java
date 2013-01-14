package edu.washington.cs.cupid.utility;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.capability.ICapability;

/**
 * Capability utility methods.
 * @author Todd Schiller
 */
public abstract class CapabilityUtil {

	private CapabilityUtil() {
		
	}
	
	/**
	 * Compares capabilities by name (ascending).
	 */
	public static Comparator<ICapability<?, ?>> COMPARE_NAME = new Comparator<ICapability<?, ?>>() {
		@Override
		public int compare(final ICapability<?, ?> lhs, final ICapability<?, ?> rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
	};
	
	/**
	 * Constructs a new list of capabilities sorted by <code>comparator</code>.
	 * @param source the source collection
	 * @param comparator sort comparator
	 * @return a new list of capabilities sorted by <code>comparator</code>.
	 */
	public static List<ICapability<?, ?>> sort(final Collection<ICapability<?, ?>> source, final Comparator<ICapability<?, ?>> comparator) {
		List<ICapability<?, ?>> result = Lists.newArrayList(source);
		Collections.sort(result, comparator);
		return result;
	}
	
}
