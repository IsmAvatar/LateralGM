/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

/** Panel to display line numbers. */
public class LineNumberPanel extends JPanel
{
	/** Shut up, ECJ. */
	private static final long serialVersionUID = 1L;

	/** The FontMetrics of the textarea that this is watching, so we know the font height for spacing line numbers */
	protected FontMetrics metrics;
	/** The number of lines to number. */
	protected int lines;
	/** Indicates whether line numbering starts at 0 */
	protected boolean startZero;

	/**
	 * @param metrics The font metrics to use to paint numbers at the correct position.
	 * @param lines The number of lines in the code.
	 * @param startZero True if the first line should be given index 0, false if it should be given index 1.
	 */
	public LineNumberPanel(FontMetrics metrics, int lines, boolean startZero)
	{
		this.metrics = metrics;
		this.lines = lines;
		this.startZero = startZero;
		resize();
	}

	/**
	 * @param textarea The text area component to draw next to.
	 * @param lines The number of lines in the text area component.
	 * @param startZero True if the first line should be given index 0, false if it should be given index 1.
	 */
	public LineNumberPanel(JComponent textarea, int lines, boolean startZero)
	{
		this(textarea.getFontMetrics(textarea.getFont()),lines,startZero);
	}

	/**
	 * Set the number of lines to be numbered.
	 * @param lines The number of lines.
	 */
	public void setLines(int lines)
	{
		this.lines = lines + (startZero ? 0 : 1);
		resize();
		repaint();
	}

	/**
	 * Call upon resize to repaint.
	 */
	public void resize()
	{
		//find the advance of the widest number
		int[] widths = getFontMetrics(getFont()).getWidths();
		int maxAdvance = 0;
		for (int i = '0'; i <= '9'; i++)
			if (widths[i] > maxAdvance) maxAdvance = widths[i];

		//multiply by max number of digits
		int width = maxAdvance * (int) Math.max(Math.log10(lines - (startZero ? 1 : 0)) + 2,2);

		//get line height, multiply by number of lines. + 1 line since the end seems to have a little extra
		int height = metrics.getHeight() * (lines + 1);

		setPreferredSize(new Dimension(width,height));
		revalidate();

		//this particular line appears to be necessary to allow these changes to take effect.
		//note that we can't swap out with validate() or revalidate() for some reason.
		//		getParent().doLayout();
	}

	/**
	 * @param g The graphics object to which to paint.
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		Object map = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //$NON-NLS-1$
		if (map != null) ((Graphics2D) g).addRenderingHints((Map<?,?>) map);

		Rectangle clip = g.getClipBounds();
		final int insetY = metrics.getLeading() + metrics.getAscent();
		final int gh = metrics.getHeight();
		int lineNum = clip.y / gh;
		final int start = lineNum * gh + insetY;
		final int end = clip.y + clip.height + gh;
		if (!startZero) lineNum++;

		g.setColor(getBackground());
		g.fillRect(clip.x,clip.y,clip.width,clip.height);
		g.setColor(getForeground());

		for (int y = start; lineNum < lines && y <= end; lineNum++, y += gh)
			g.drawString(Integer.toString(lineNum),0,y);
	}
}
