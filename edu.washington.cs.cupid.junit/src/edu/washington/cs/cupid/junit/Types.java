package edu.washington.cs.cupid.junit;

import java.util.Set;

import org.eclipse.jdt.internal.junit.model.TestElement;

import com.google.common.reflect.TypeToken;

@SuppressWarnings("restriction")
public final class Types {
	
	public final static TypeToken<Set<TestElement>> TEST_ELEMENTS = new TypeToken<Set<TestElement>>(){
		private static final long serialVersionUID = 1L;
	};
	
	private Types(){
		// NO OP
	}
}
