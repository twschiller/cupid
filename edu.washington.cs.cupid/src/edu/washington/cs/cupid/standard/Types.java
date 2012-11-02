package edu.washington.cs.cupid.standard;

import java.util.Collection;

import com.google.common.reflect.TypeToken;

/**
 * Standard Type Tokens
 * @author Todd Schiller
 */
public final class Types {

	private Types(){
		// NO OP
	};
	
	public static final TypeToken<Collection<?>> GENERIC_COLLECTION = new TypeToken<Collection<?>>(){
		private static final long serialVersionUID = 1L;		
	};
	

}
