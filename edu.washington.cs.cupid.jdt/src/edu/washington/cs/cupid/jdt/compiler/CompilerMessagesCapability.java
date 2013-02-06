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
package edu.washington.cs.cupid.jdt.compiler;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.linear.AbstractLinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;

/**
 * Capability that returns the compiler messages for a compilation unit.
 * @author Todd Schiller
 */
public final class CompilerMessagesCapability extends AbstractLinearCapability<ICompilationUnit, List<Message>> {
		
	/**
	 * Construct a capability that returns the compiler messages for a compilation unit.
	 */
	public CompilerMessagesCapability() {
		super("Compiler Messages", 
			  "edu.washington.cs.cupid.jdt.messages", 
			  "Compiler messages (e.g., warnings and errors)",
			  TypeToken.of(ICompilationUnit.class), new TypeToken<List<Message>>() {},
			  Flag.PURE);
	}
	
	@Override
	public LinearJob<ICompilationUnit, List<Message>> getJob(final ICompilationUnit input) {
		return new LinearJob<ICompilationUnit, List<Message>>(this, input) {
			@Override
			protected LinearStatus<List<Message>> run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask(getName(), 100);
					CompilationUnit unit = ParseUtil.parse(input, new SubProgressMonitor(monitor, 100));	
					return LinearStatus.makeOk(getCapability(), Arrays.asList(unit.getMessages()));
				} catch (Exception ex) {
					return LinearStatus.<List<Message>>makeError(ex);
				} finally {
					monitor.done();
				}
			}
		};
	}
}
