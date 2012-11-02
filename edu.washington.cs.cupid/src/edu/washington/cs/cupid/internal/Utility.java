package edu.washington.cs.cupid.internal;

/**
 * Static utility methods.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class Utility {
	
	/**
	 * @return the first non-<code>null</code> argument, or <code>null</code> iff all
	 * the arguments are null.
	 */
	public static <T> T coalesce(T... argv) {
	    for(T i : argv) if(i != null) return i;
	    return null;
	}
	
	/**
	 * @return the first non-<code>null</code> argument, or <code>null</code> iff all
	 * the arguments are null.
	 */
	public static <T> T coalesce(T x, T y) {
	    return x == null ? y : x;
	}
	
	/**
	 * @return the first non-<code>null</code> argument, or <code>null</code> iff all
	 * the arguments are null.
	 */
	public static <T> T coalesce(T x, T y, T z) {
	    return x != null ? x : (y != null ? y : z);
	}

}
