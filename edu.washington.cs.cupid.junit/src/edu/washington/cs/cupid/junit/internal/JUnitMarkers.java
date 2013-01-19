package edu.washington.cs.cupid.junit.internal;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.model.TestCaseElement;
import org.eclipse.jdt.internal.junit.model.TestElement;
import org.eclipse.jdt.internal.junit.model.TestRunSession;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.markers.IMarkerBuilder;
import edu.washington.cs.cupid.markers.MarkerBuilder;

/**
 * A capability that produces problem markers for failed JUnit tests in a test session.
 * @see {@link TestRunSession}
 * @see {@link IMarkerBuilder}
 * @author Todd Schiller
 */
@SuppressWarnings("restriction")
public final class JUnitMarkers extends AbstractCapability<TestRunSession, Collection<IMarkerBuilder>> {

	/**
	 * Construct a capability that produces problem markers for failed JUnit tests in a test session.
	 */
	public JUnitMarkers() {
		super("JUnit Marker Builder",
			  "edu.washington.cs.cupid.junit.internal.markers",
			  "Create problem markers from failed tests",
			  TypeToken.of(TestRunSession.class),
			  IMarkerBuilder.MARKER_RESULT,
			  Flag.PURE, Flag.LOCAL);
	}

	@Override
	public CapabilityJob<TestRunSession, Collection<IMarkerBuilder>> getJob(final TestRunSession input) {
		return new CapabilityJob<TestRunSession, Collection<IMarkerBuilder>>(this, input) {
			@Override
			protected CapabilityStatus<Collection<IMarkerBuilder>> run(final IProgressMonitor monitor) {
				monitor.beginTask("Building JUnit Markers", input.getAllFailedTestElements().length);
				Collection<IMarkerBuilder> result = Sets.newHashSet();
				
				for (TestElement element : input.getAllFailedTestElements()) {
					if (element instanceof TestCaseElement) {
						TestCaseElement test = (TestCaseElement) element;
						
						IResource resource = null;
						IMethod method = null;
						
						try {
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
						} catch (JavaModelException e) {
							monitor.done();
							return CapabilityStatus.makeError(e);
						}
						
						if (resource != null) {
							MarkerBuilder builder = new MarkerBuilder(resource)
								.set(IMarker.MESSAGE, "Test failed: " + element.getTestName())
								.set(IMarker.TRANSIENT, true)
								.set(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
							
							if (method != null) {
								try {
									builder
										.set(IMarker.CHAR_START, method.getSourceRange().getOffset())
										.set(IMarker.CHAR_END, method.getSourceRange().getOffset() + method.getSourceRange().getLength());
								} catch (JavaModelException e) {
									monitor.done();
									return CapabilityStatus.makeError(e);
								}
							}

							result.add(builder);
						}
					}
				
					monitor.worked(1);
				}
				monitor.done();
				return CapabilityStatus.makeOk(result);
			}
		};
	}

}
