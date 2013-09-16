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
package edu.washington.cs.cupid.mylyn;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * Capability that returns all Mylyn tasks.
 * @author Todd Schiller
 */
public final class MylynTaskCapability extends LinearCapability<Void, List<AbstractTask>> {

	// http://wiki.eclipse.org/Mylyn_Integrator_Reference#Integrating_with_Mylyn.27s_Task_List_vs._using_a_custom_view
	
	/**
	 * Construct a capability that returns all Mylyn tasks.
	 */
	public MylynTaskCapability() {
		super("Mylyn Tasks",
			  "All Mylyn tasks",
			  TypeToken.of(Void.class), new TypeToken<List<AbstractTask>>() {},
			  Flag.PURE);
	}

	@Override
	public LinearJob<Void, List<AbstractTask>> getJob(final Void input) {
		return new LinearJob<Void, List<AbstractTask>>(this, input) {
			@Override
			protected LinearStatus<List<AbstractTask>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
					TaskList taskList = TasksUiPlugin.getTaskList();
					List<AbstractTask> tasks = new ArrayList<AbstractTask>(taskList.getAllTasks());
					return LinearStatus.makeOk(getCapability(), tasks);
				} catch (Exception ex) {
					return LinearStatus.<List<AbstractTask>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
