package edu.washington.cs.cupid.shadow;

/**
 * A job that returns a reference to the resource or element in a shadow copy
 * of the project
 * @author Todd Schiller (tws@cs.washington.edu)
 * @param <T> element or resource type
 */
public interface IShadowJob<T> {
	
	/**
	 * @return reference in the shadow project
	 */
	T get();
}
