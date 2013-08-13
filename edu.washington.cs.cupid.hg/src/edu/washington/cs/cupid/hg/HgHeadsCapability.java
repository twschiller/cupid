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
package edu.washington.cs.cupid.hg;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.model.ChangeSet;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * A capability that returns the Hg heads for a resource.
 * @author Todd Schiller
 */
public final class HgHeadsCapability extends LinearCapability<IResource, List<ChangeSet>> {

	/**
	 * Construct a capability that returns the Hg heads for a resource.
	 */
	public HgHeadsCapability() {
		super("Hg Heads",
			  "Hg heads for the resource's repository",
			  TypeToken.of(IResource.class), new TypeToken<List<ChangeSet>>(){}, 
			  Flag.PURE);
	}
	
	@Override
	public LinearJob<IResource, List<ChangeSet>> getJob(final IResource input) {
		return new LinearJob<IResource, List<ChangeSet>>(this, input) {
			@Override
			protected LinearStatus<List<ChangeSet>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(input);
					
					if (root == null) {
						return LinearStatus.<List<ChangeSet>>makeError(new ResourceNotHgVersionedException(input));
					}
					
					List<ChangeSet> result = 
							Lists.newArrayList(HgLogClient.getChangeSets(root, HgLogClient.getHeads(root)));
					return LinearStatus.makeOk(getCapability(), result);
				} catch (Exception ex) {
					return LinearStatus.<List<ChangeSet>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
