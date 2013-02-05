/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.wizards.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;

/**
 * Handles serialization and deserialization
 * @author Todd Schiller
 */
public class HydrationService {

	public HydrationService() {
		super();
	}

	/**
	 * Create a clean version of the capability name by converting non-alphanumeric
	 * characters to underscores
	 * @param capability the capability
	 * @return a version of the capability name that is a valid file name
	 */
	public static String cleanName(ICapability<?,?> capability){
		char result[] = new char[capability.getName().length()];
		char old[] = capability.getName().toCharArray();
		
		for (int i = 0; i < old.length; i++){
			result[i] = Character.isDigit(old[i]) || Character.isLetter(old[i])
					? old[i]
					: '_';
		}
		
		return new String(result);
	}
	
	public ICapability<?,?> hydrate(File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream(file));
		return (ICapability<?,?> ) fileIn.readObject();
	}
	
	public <I extends ICapability & Serializable> void store(I capability) throws IOException{
		File file = new File(CupidPlatform.getPipelineDirectory(), cleanName(capability) + ".arrow");
		ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(file));
		writer.writeObject(capability);
		writer.close();
	}
}
