package edu.washington.cs.cupid.chart.internal;

import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;

public class IsNumericTester extends PropertyTester {

	public IsNumericTester(){
		// NO OP
	}
	
	private static final Set<TypeToken<?>> NUMERIC = Sets.<TypeToken<?>>newHashSet(
			TypeToken.of(Number.class),
			TypeToken.of(int.class),
			TypeToken.of(double.class),
			TypeToken.of(float.class),
			TypeToken.of(long.class));
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		ICapability capability = (ICapability) receiver;
		
		if ("isNumeric".equals("property")){		
			boolean r = false;
			
			if (CapabilityUtil.isLinear(capability)){
				r = false;
			}else{
				TypeToken<?> retType = CapabilityUtil.singleOutput(capability).getType();
				
				for (TypeToken<?> t : NUMERIC){
					if (TypeManager.isJavaCompatible(t, retType)){
						r = true;
						break;
					}
				}
			}
			
			return expectedValue == null ? r : (r == (Boolean) expectedValue);
		}
		
		Assert.isTrue(false);
		return false;
	}

}
