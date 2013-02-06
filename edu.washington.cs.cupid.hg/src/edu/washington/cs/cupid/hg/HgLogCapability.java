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
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.model.JHgChangeSet;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import edu.washington.cs.cupid.capability.AbstractBaseCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapabilityArguments;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.Parameter;

/**
 * A capability that returns the Hg log for a resource.
 * @author Todd Schiller
 */
public final class HgLogCapability extends AbstractBaseCapability {

	public static final Parameter<Integer> PARAM_MAX_ENTRIES = new Parameter<Integer>("Maximum Entries", Integer.class, 100);
	public static final Parameter<Integer> PARAM_START_REVISION = new Parameter<Integer>("Start Revision", Integer.class, 0);
	public static final Parameter<IResource> PARAM_RESOURCE = new Parameter<IResource>("Resource", IResource.class);
	
	public static final Output<List<JHgChangeSet>> OUT_LOG = new Output<List<JHgChangeSet>>("Log", new TypeToken<List<JHgChangeSet>>(){});
	
	/**
	 * Construct a capability that returns the Hg log for a resource.
	 */
	public HgLogCapability() {
		super("Hg Log",
			  "edu.washington.cs.cupid.hg.log",
			  "Hg log entries for the resource",
			  Lists.<Parameter<?>>newArrayList(PARAM_RESOURCE, PARAM_MAX_ENTRIES, PARAM_START_REVISION),
			  Lists.<Output<?>>newArrayList(OUT_LOG),
			  Flag.PURE, Flag.TRANSIENT);
	}


	@Override
	public CapabilityJob<AbstractBaseCapability> getJob(final ICapabilityArguments input) {
		return new CapabilityJob<AbstractBaseCapability> (this, input){
			@Override
			protected CapabilityStatus run(final IProgressMonitor monitor) {
				try{
					IResource resource = input.getValueArgument(PARAM_RESOURCE);
					int maxEntries = input.getValueArgument(PARAM_MAX_ENTRIES);
					int startRevision = input.getValueArgument(PARAM_START_REVISION);
					
					monitor.beginTask("Getting Hg log for resource " + resource.getName(), 1);
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(resource);
					
					if (root == null){
						return CapabilityStatus.makeError(new ResourceNotHgVersionedException(resource));
					}
					
					List<JHgChangeSet> result = HgLogClient.getResourceLog(root, resource, maxEntries, startRevision);
					return CapabilityStatus.makeOk(CapabilityUtil.packSingleOutputValue(OUT_LOG, result));
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}finally{
					monitor.done();
				}
			}
		};
	}
}
