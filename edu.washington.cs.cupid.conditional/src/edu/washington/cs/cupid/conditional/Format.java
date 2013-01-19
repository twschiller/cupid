package edu.washington.cs.cupid.conditional;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * A format override, optionally specifying foreground color, background color, and font.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public final class Format {
	private RGB foreground = null;
	private RGB background = null;
	private FontData[] font = null;
	
	/**
	 * Construct a new format with no overrides.
	 */
	public Format() {
		// NO OP
	}
	
	/**
	 * Construct a format override.
	 * @param foreground the foreground color override, or <code>null</code>
	 * @param background the background color override, or <code>null</code>
	 * @param font the font override, or <code>null</code>
	 */
	public Format(final RGB foreground, final RGB background, final FontData[] font) {
		this.foreground = foreground;
		this.background = background;
		this.font = font;
	}

	/**
	 * @return the foreground to color to apply, or <code>null</code>.
	 */
	public RGB getForeground() {
		return foreground;
	}

	/**
	 * @return the background color to apply, or <code>null</code>.
	 */
	public RGB getBackground() {
		return background;
	}

	/**
	 * @return the font to apply, or <code>null</code>.
	 */
	public FontData[] getFont() {
		return font;
	}

	/**
	 * @param foreground the foreground to color to apply, or <code>null</code>.
	 */
	public void setForeground(final RGB foreground) {
		this.foreground = foreground;
	}

	/**
	 * @param background the background color to apply, or <code>null</code>.
	 */
	public void setBackground(final RGB background) {
		this.background = background;
	}

	/**
	 * @param font the font to apply, or <code>null</code>.
	 */
	public void setFont(final FontData[] font) {
		this.font = font;
	}
}
