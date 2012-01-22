/* Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of JoshEdit. JoshEdit is free software.
 * You can use, modify, and distribute it under the terms of
 * the GNU General Public License, version 3 or later. 
 */

package org.lateralgm.joshedit;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.JPanel;

public class LineNumberPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected int lines;
	/** Indicates whether line numbering starts at 0 */
	protected boolean startZero = true;
	/** The font of the textarea that this is watching, so we know the font height for spacing line numbers */
	protected Font metricFont;

	public LineNumberPanel(Font metricFont, int lines)
	{
		this.metricFont = metricFont;
		this.lines = lines;
		resize();
	}

	public void setLines(int lines)
	{
		this.lines = lines + (startZero ? 0 : 1);
		resize();
		repaint();
	}

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
		int height = getFontMetrics(metricFont).getHeight() * (lines + 1);

		setPreferredSize(new Dimension(width,height));
		revalidate();

		//this particular line appears to be necessary to allow these changes to take effect.
		//note that we can't swap out with validate() or revalidate() for some reason.
		//		getParent().doLayout();
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Object map = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //$NON-NLS-1$
		if (map != null) ((Graphics2D) g).addRenderingHints((Map<?,?>) map);
		
		Rectangle clip = g.getClipBounds();
		FontMetrics fm = getFontMetrics(metricFont);
		final int insetY = fm.getLeading() + fm.getAscent();
		final int gh = fm.getHeight();
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
