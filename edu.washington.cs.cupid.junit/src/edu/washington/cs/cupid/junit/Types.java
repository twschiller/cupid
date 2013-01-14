package edu.washington.cs.cupid.junit;

import java.util.Set;

import org.eclipse.jdt.internal.junit.model.TestElement;

import com.google.common.reflect.TypeToken;

/**
 * Types used by the JUnit Cupid plugin.
 * @author Todd Schiller
 */
@SuppressWarnings("restriction")
public final class Types {
	
	/**
	 * Token for a set of {@link TestElement}s.
	 */
	public static final TypeToken<Set<TestElement>> TEST_ELEMENTS = new TypeToken<Set<TestElement>>() {
		private static final long serialVersionUID = 1L;
	};
	
	private Types() {
		// NO OP
	}
}
