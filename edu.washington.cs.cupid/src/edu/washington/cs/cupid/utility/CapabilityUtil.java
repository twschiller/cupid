package edu.washington.cs.cupid.utility;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.capability.ICapability;

public abstract class CapabilityUtil {

	public static Comparator<ICapability<?,?>> COMPARE_NAME = new Comparator<ICapability<?,?>>(){
		@Override
		public int compare(ICapability<?, ?> lhs, ICapability<?, ?> rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
	};
	
	public static List<ICapability<?,?>> sort(Collection<ICapability<?,?>> xs, Comparator<ICapability<?,?>> comparator){
		List<ICapability<?,?>> result = Lists.newArrayList(xs);
		Collections.sort(result, comparator);
		return result;
	}
	
}
