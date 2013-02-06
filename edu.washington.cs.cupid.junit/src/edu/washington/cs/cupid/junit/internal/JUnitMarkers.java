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
package edu.washington.cs.cupid.junit.internal;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.model.TestCaseElement;
import org.eclipse.jdt.internal.junit.model.TestElement;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.linear.LinearCapability;
import edu.washington.cs.cupid.capability.linear.LinearJob;
import edu.washington.cs.cupid.capability.linear.LinearStatus;
import edu.washington.cs.cupid.markers.IMarkerBuilder;
import edu.washington.cs.cupid.markers.MarkerBuilder;

/**
 * A capability that produces problem markers for failed JUnit tests in a test session.
 * @see {@link TestRunSession}
 * @see {@link IMarkerBuilder}
 * @author Todd Schiller
 */
@SuppressWarnings("restriction")
public final class JUnitMarkers extends LinearCapability<TestRunSession, Collection<IMarkerBuilder>> {

	/**
	 * Construct a capability that produces problem markers for failed JUnit tests in a test session.
	 */
	public JUnitMarkers() {
		super("JUnit Marker Builder",
			  "edu.washington.cs.cupid.junit.internal.markers",
			  "Create problem markers from failed tests",
			  TypeToken.of(TestRunSession.class), IMarkerBuilder.MARKER_RESULT,
			  Flag.PURE);
	}

	@Override
	public LinearJob<TestRunSession, Collection<IMarkerBuilder>> getJob(final TestRunSession input) {
		return new LinearJob<TestRunSession, Collection<IMarkerBuilder>>(this, input) {
			@Override
			protected LinearStatus<Collection<IMarkerBuilder>> run(final IProgressMonitor monitor) {
				monitor.beginTask("Building JUnit Markers", input.getAllFailedTestElements().length);
				Collection<IMarkerBuilder> result = Sets.newHashSet();
				
				try {

					for (TestElement element : input.getAllFailedTestElements()) {
						if (element instanceof TestCaseElement) {
							TestCaseElement test = (TestCaseElement) element;

							IResource resource = null;
							IMethod method = null;

							IType type = test.getTestRunSession().getLaunchedProject().findType(test.getClassName());

							if (type == null) {
								resource = test.getTestRunSession().getLaunchedProject().getCorrespondingResource();
							} else {
								ICompilationUnit cunit = type.getCompilationUnit();
								method = type.getMethod(test.getTestMethodName(), new String[]{});

								if (cunit == null) {
									resource = test.getTestRunSession().getLaunchedProject().getCorrespondingResource();
								} else {
									resource = cunit.getCorrespondingResource();
								}
							}

							if (resource != null) {
								MarkerBuilder builder = new MarkerBuilder(resource)
									.set(IMarker.MESSAGE, "Test failed: " + element.getTestName())
									.set(IMarker.TRANSIENT, true)
									.set(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);

								if (method != null) {
									builder
										.set(IMarker.CHAR_START, method.getSourceRange().getOffset())
										.set(IMarker.CHAR_END, method.getSourceRange().getOffset() + method.getSourceRange().getLength());
								}

								result.add(builder);
							}
						}

						monitor.worked(1);
					}
					return LinearStatus.makeOk(getCapability(), result);
				} catch (Exception e){
					return LinearStatus.<Collection<IMarkerBuilder>>makeError(e);
				} finally {
					monitor.done();
				}	
			}
		};
	}

}
