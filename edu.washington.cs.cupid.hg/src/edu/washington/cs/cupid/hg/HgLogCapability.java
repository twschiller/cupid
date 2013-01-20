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

import com.google.common.reflect.TypeToken;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.model.JHgChangeSet;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;

public class HgLogCapability extends GenericAbstractCapability<IResource, List<JHgChangeSet>> {

	public static final int RESOURCE_LOG_LIMIT = 100;
	
	public HgLogCapability() {
		super("Hg Log",
			  "edu.washington.cs.cupid.hg.log",
			  "Hg log entries for the resource",
			  Flag.PURE, Flag.TRANSIENT);
	}

	@Override
	public TypeToken<IResource> getParameterType() {
		return TypeToken.of(IResource.class);
	}

	@Override
	public TypeToken<List<JHgChangeSet>> getReturnType() {
		return new TypeToken<List<JHgChangeSet>>(getClass()){};
	}

	@Override
	public CapabilityJob<IResource, List<JHgChangeSet>> getJob(IResource input) {
		return new CapabilityJob<IResource, List<JHgChangeSet>>(this, input){
			@Override
			protected CapabilityStatus<List<JHgChangeSet>> run(IProgressMonitor monitor) {
				monitor.beginTask("Getting Hg log for resource " + input.getName(), 1);
				try{
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(input);
					
					if (root == null){
						return CapabilityStatus.makeError(new ResourceNotHgVersionedException(input));
					}
					
					List<JHgChangeSet> result = HgLogClient.getResourceLog(root, input, RESOURCE_LOG_LIMIT, 0);
					return CapabilityStatus.makeOk(result);
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}finally{
					monitor.done();
				}
			}
		};
	}
}
