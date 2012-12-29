package edu.washington.cs.cupid.conditional;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * A format override, optionally specifying foreground color, background color, and font.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class Format {
	private RGB foreground = null;
	private RGB background = null;
	private FontData[] font = null;
	
	/**
	 * Construct a new format with no overrides.
	 */
	public Format(){
		// NO OP
	}
	
	public Format(RGB foreground, RGB background, FontData[] font){
		this.foreground = foreground;
		this.background = background;
		this.font = font;
	}

	/**
	 * @return the foreground to color to apply, or <code>null</code>
	 */
	public RGB getForeground() {
		return foreground;
	}

	/**
	 * @return the background color to apply, or <code>null</code>
	 */
	public RGB getBackground() {
		return background;
	}

	/**
	 * @return the font to apply, or <code>null</code>
	 */
	public FontData[] getFont() {
		return font;
	}

	/**
	 * @param foreground the foreground to color to apply, or <code>null</code>
	 */
	public void setForeground(RGB foreground) {
		this.foreground = foreground;
	}

	/**
	 * @param background the background color to apply, or <code>null</code>
	 */
	public void setBackground(RGB background) {
		this.background = background;
	}

	/**
	 * @param font the font to apply, or <code>null</code>
	 */
	public void setFont(FontData[] font) {
		this.font = font;
	}
}
