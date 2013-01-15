package edu.washington.cs.cupid.internal;

/**
 * Static utility methods.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class Utility {
	
	private Utility() {
		
	}
	
	/**
	 * @return the first non-<code>null</code> argument, or <code>null</code> iff all
	 * the arguments are null.
	 * @param argv the arguments
	 * @param <T> the argument type
	 */
	public static <T> T coalesce(final T... argv) {
	    for (T i : argv) {
	    	if (i != null) {
	    		return i;
	    	}
	    }
	    return null;
	}
	
	/**
	 * @return the first non-<code>null</code> argument, or <code>null</code> iff all
	 * the arguments are null.
	 * @param x the first argument
	 * @param y the second argument
	 * @param <T> the argument type
	 */
	public static <T> T coalesce(final T x, final T y) {
	    return x == null ? y : x;
	}
	
	/**
	 * @return the first non-<code>null</code> argument, or <code>null</code> iff all
	 * the arguments are null.
	 * @param x the first argument
	 * @param y the second argument
	 * @param z the third argument
	 * @param <T> the argument type
	 */
	public static <T> T coalesce(final T x, final T y, final T z) {
	    return x != null ? x : (y != null ? y : z);
	}

}
