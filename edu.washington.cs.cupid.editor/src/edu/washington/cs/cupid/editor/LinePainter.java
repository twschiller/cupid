package edu.washington.cs.cupid.editor;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.CapabilityExecutor;
import edu.washington.cs.cupid.capability.CapabilityStatus;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.editor.preferences.PreferenceConstants;
import edu.washington.cs.cupid.jobs.NullJobListener;

public class LinePainter implements IDocumentListener, IPropertyChangeListener{

	/** Ranges to draw **/
	private Map<LineProvider, Collection<ILineRange>> ranges = Maps.newConcurrentMap();
	
	/** Currently running capabilities **/
	private Set<LineProvider> running = Sets.newHashSet();
	
	/** Capabilities to rerun **/
	private Set<LineProvider> pending = Sets.newHashSet();
	
	/** Capabilities providing text range output **/
	private List<LineProvider> providers = Lists.newArrayList();
	
	/** The vertical ruler column that delegates painting to this painter. */
	private final IVerticalRulerColumn fColumn;
	/** The parent ruler. */
	private CompositeRuler fParentRuler;
	/** The column's control, typically a {@link Canvas}, possibly <code>null</code>. */
	private Control fControl;
	/** The text viewer that the column is attached to. */
	private ITextViewer fViewer;
	/** The viewer's text widget. */
	private StyledText fWidget;
	
	/**
	 * Creates a new diff painter for a vertical ruler column.
	 *
	 * @param column the column that will delegate{@link #paint(GC, ILineRange) painting} to the
	 *        newly created painter.
	 * @param sharedColors a shared colors object to store shaded colors in, may be
	 *        <code>null</code>
	 */
	public LinePainter(IVerticalRulerColumn column) {
		Assert.isLegal(column != null);
		fColumn= column;
		providers = RulerUtil.allLineProviders(Activator.getDefault().getRulerPreferences(), true);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}
	
	/**
	 * Sets the parent ruler - the delegating column must call this method as soon as it creates its
	 * control.
	 *
	 * @param parentRuler the parent ruler
	 */
	public void setParentRuler(CompositeRuler parentRuler) {
		fParentRuler= parentRuler;
	}
	
	/**
	 * Delegates the painting of the quick diff colors to this painter. The painter will draw the
	 * color boxes onto the passed {@link GC} for all model (document) lines in
	 * <code>visibleModelLines</code>.
	 *
	 * @param gc the {@link GC} to draw onto
	 * @param visibleModelLines the lines (in document offsets) that are currently (perhaps only
	 *        partially) visible
	 */
	public void paint(GC gc, ILineRange visibleModelLines, Color color) {
		connectIfNeeded();
		if (!isConnected())
			return;

		// draw diff info
		final int lastLine= end(visibleModelLines);
		final int width= getWidth();
		for (int line= visibleModelLines.getStartLine(); line < lastLine; line++) {
			paintLine(line, gc, width, color);
		}
	}
	
	public void paintRanges(GC gc, Display display, Color defaultColor){
		synchronized (running) {
			for (LineProvider provider : ranges.keySet()){
				for (ILineRange range : ranges.get(provider)){
					Color c = provider.getColor() == null ? defaultColor : new Color(display, provider.getColor()); 
					paint(gc, range, c);
				}
			}
		}
	}
	
	/**
	 * Paints a single model line onto <code>gc</code>.
	 *
	 * @param line the model line to paint
	 * @param gc the {@link GC} to paint onto
	 * @param width the width of the column
	 */
	private void paintLine(int line, GC gc, int width, Color color) {
		int widgetLine= JFaceTextUtil.modelLineToWidgetLine(fViewer, line);
		if (widgetLine == -1)
			return;

		int y= fWidget.getLinePixel(widgetLine);
		int lineHeight= fWidget.getLineHeight(fWidget.getOffsetAtLine(widgetLine));

		// draw background color if special
		gc.setBackground(color);
		gc.fillRectangle(0, y, width, lineHeight);
	}
	
	/**
	 * Ensures that the column is fully instantiated, i.e. has a control, and that the viewer is
	 * visible.
	 */
	private void connectIfNeeded() {
		if (isConnected() || fParentRuler == null)
			return;

		fViewer= fParentRuler.getTextViewer();
		if (fViewer == null)
			return;

		fWidget= fViewer.getTextWidget();
		if (fWidget == null)
			return;

		fControl= fColumn.getControl();
		if (fControl == null)
			return;

		fControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleDispose();
			}
		});
	}

	/**
	 * Returns <code>true</code> if the column is fully connected.
	 *
	 * @return <code>true</code> if the column is fully connected, false otherwise
	 */
	private boolean isConnected() {
		return fControl != null;
	}
	
	/**
	 * Disposes of this painter and releases any resources.
	 */
	private void handleDispose() {
		// NOP
	}
	
	private void runProvider(LineProvider provider){
		Object input = null;
		
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IDocument document= fViewer.getDocument();
		
		// TODO: we should use type adapters here
		if (RulerUtil.isDocumentCapability(provider.getCapability())){
			input = document;
		}else{
			ITextFileBuffer buffer= bufferManager.getTextFileBuffer(document);
			if (RulerUtil.isTextBufferCapability(provider.getCapability())){
				input = buffer;
			}else if (RulerUtil.isFileCapability(provider.getCapability())){
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(buffer.getLocation());
				input = file;
			}else{
				throw new IllegalArgumentException(
						"Capability input type not supported by ruler:" +
					    CapabilityUtil.unaryParameter(provider.getCapability()).getType().toString());
			}
		}
		
		synchronized (running) {
			if (!running.contains(provider)){
				
				running.add(provider);
				pending.remove(provider);
				
				CapabilityExecutor.asyncExec(provider.getCapability(), 
						CapabilityUtil.packUnaryInput(provider.getCapability(), input), 
						LinePainter.class, new PaintJobListener(provider));
			}else{
				pending.add(provider);
			}
		}
	}
	
	private class PaintJobListener extends NullJobListener{
		private LineProvider provider;
		
		public PaintJobListener(LineProvider provider) {
			this.provider = provider;
		}

		@Override
		public void done(IJobChangeEvent event) {
			CapabilityStatus status = (CapabilityStatus) event.getResult();
			Collection<ILineRange> value = (Collection<ILineRange>) status.value().getOutput(provider.getOutput());
					
			synchronized (running) {
				ranges.put(provider, value);
				running.remove(provider);
				runPending();
			}
			
			postRedraw();
		}
	}
	
	/**
	 * Triggers a redraw in the display thread.
	 */
	private final void postRedraw() {
		if (isConnected() && !fControl.isDisposed()) {
			Display d= fControl.getDisplay();
			if (d != null) {
				d.asyncExec(new Runnable() {
					public void run() {
						redraw();
					}
				});
			}
		}
	}
	
	/**
	 * Computes the end index of a line range.
	 *
	 * @param range a line range
	 * @return the last line (exclusive) of <code>range</code>
	 */
	private static int end(ILineRange range) {
		return range.getStartLine() + range.getNumberOfLines();
	}

	/**
	 * Triggers redrawing of the column.
	 */
	private void redraw() {
		fColumn.redraw();
	}

	/**
	 * Returns the width of the column.
	 *
	 * @return the width of the column
	 */
	private int getWidth() {
		return fColumn.getWidth();
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// NOP
	}

	private void runAll(){
		synchronized (running) {
			for (LineProvider provider : providers){
				runProvider(provider);
			}
		}
	}
	
	private void runPending(){
		synchronized (running) {
			for (LineProvider provider : pending){
				runProvider(provider);
			}
		}
	}
	
	@Override
	public void documentChanged(DocumentEvent event) {
		runAll();
	}


	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_RULER_PREFERENCES)){
			synchronized (running) {
				ranges.clear();
				providers = RulerUtil.allLineProviders(Activator.getDefault().getRulerPreferences(), true);
				runAll();
			}
		}
	}
}

