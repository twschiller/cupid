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
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.vectrace.MercurialEclipse.commands.HgLogClient;
import com.vectrace.MercurialEclipse.model.HgRoot;
import com.vectrace.MercurialEclipse.model.JHgChangeSet;
import com.vectrace.MercurialEclipse.team.cache.MercurialRootCache;

import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.GenericAbstractCapability;
import edu.washington.cs.cupid.capability.options.IConfigurableCapability;
import edu.washington.cs.cupid.capability.options.Option;
import edu.washington.cs.cupid.capability.options.OptionManager;

/**
 * A capability that returns the Hg log for a resource.
 * @author Todd Schiller
 */
public final class HgLogCapability extends GenericAbstractCapability<IResource, List<JHgChangeSet>> implements IConfigurableCapability<IResource, List<JHgChangeSet>> {

	public static final String RESOURCE_LOG_LIMIT_KEY = "Entry Limit";
	public static final int RESOURCE_LOG_LIMIT_DEFAULT = 100;
	
	public static final String START_REVISION_KEY = "Start Revision";
	public static final int START_REVISION_DEFAULT = 0;
	
	
	public static final OptionManager optionManager;
	static {
		optionManager = new OptionManager(new Option<?>[] {
				new Option<Integer>(START_REVISION_KEY, Integer.class, Integer.valueOf(START_REVISION_DEFAULT)),
				new Option<Integer>(RESOURCE_LOG_LIMIT_KEY, Integer.class, Integer.valueOf(RESOURCE_LOG_LIMIT_DEFAULT)),
		});
	}
	
	/**
	 * Construct a capability that returns the Hg log for a resource.
	 */
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
	public CapabilityJob<IResource, List<JHgChangeSet>> getJob(final IResource input) {
		return getJob(input, Maps.<String, Object>newHashMap());
	}

	@Override
	public CapabilityJob<IResource, List<JHgChangeSet>> getJob(final IResource input, final Map<String, Object> options) {
		return new CapabilityJob<IResource, List<JHgChangeSet>>(this, input){
			@Override
			protected CapabilityStatus<List<JHgChangeSet>> run(final IProgressMonitor monitor) {
				try{
					monitor.beginTask("Getting Hg log for resource " + input.getName(), 1);
					HgRoot root = MercurialRootCache.getInstance().getHgRoot(input);
					
					if (root == null){
						return CapabilityStatus.makeError(new ResourceNotHgVersionedException(input));
					}
					
					List<JHgChangeSet> result = HgLogClient.getResourceLog(
							root, input, 
							(Integer) optionManager.getValue(RESOURCE_LOG_LIMIT_KEY, options), 
							(Integer) optionManager.getValue(START_REVISION_KEY, options));
					return CapabilityStatus.makeOk(result);
				}catch(Exception ex){
					return CapabilityStatus.makeError(ex);
				}finally{
					monitor.done();
				}
			}
		};
	}
	
	@Override
	public List<Option<?>> getOptions() {
		return optionManager.getOptions();
	}

	@Override
	public Option<?> getOption(String name) throws IllegalArgumentException {
		return optionManager.getOption(name);
	}

}
