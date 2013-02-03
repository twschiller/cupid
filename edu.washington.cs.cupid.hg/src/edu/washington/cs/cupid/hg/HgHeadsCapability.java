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

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;

/**
 * A capability that returns the Hg heads for a resource.
 * @author Todd Schiller
 */
public final class HgHeadsCapability extends GenericAbstractCapability<IResource, List<ChangeSet>> {

	/**
	 * Construct a capability that returns the Hg heads for a resource.
	 */
	public HgHeadsCapability() {
		super("Hg Heads",
			  "edu.washington.cs.cupid.hg.heads",
			  "Hg heads for the resource's repository",
			  Flag.PURE, Flag.TRANSIENT);
	}
	
	@Override
	public TypeToken<IResource> getParameterType() {
		return TypeToken.of(IResource.class);
	}

	@SuppressWarnings("serial")
	@Override
	public TypeToken<List<ChangeSet>> getReturnType() {
		return new TypeToken<List<ChangeSet>>(getClass()){};
	}

	@Override
	public CapabilityJob<IResource, List<ChangeSet>> getJob(final IResource input) {
		return new CapabilityJob<IResource, List<ChangeSet>>(this, input) {
			@Override
			protected CapabilityStatus<List<ChangeSet>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 1);
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(input);
					
					if (root == null) {
						return CapabilityStatus.makeError(new ResourceNotHgVersionedException(input));
					}
					
					List<ChangeSet> result = 
							Lists.newArrayList(HgLogClient.getChangeSets(root, HgLogClient.getHeads(root)));
					return CapabilityStatus.makeOk(result);
				} catch (Exception ex) {
					return CapabilityStatus.makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}

}
