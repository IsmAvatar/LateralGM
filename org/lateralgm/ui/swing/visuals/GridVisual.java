/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

public class GridVisual implements Visual
	{
	private static final Color GRID_DARK = new Color(0,0,0,96);
	private static final Color GRID_BRIGHT = new Color(255,255,255,96);

	private boolean rhombic;
	private int width, height;

	/*
	 * Drawing grid lines with alpha appears to be incredibly slow (at least for a 16x16 grid),
	 * so for grid sizes that aren't too large, it is drawn to an image which is then repeated over
	 * the screen.
	 * TODO: For small grids, make it cover multiple grid units to avoid repeating too much.
	 * 	     For large grids, maybe cache only the edges.
	 */
	private SoftReference<BufferedImage> gridImage;

	private final Component component;

	public GridVisual(Component c, boolean r, int w, int h)
		{
		component = c;
		rhombic = r;
		width = w;
		height = h;
		}

	public void setRhombic(boolean r)
		{
		if (r == rhombic) return;
		rhombic = r;
		gridImage = null;
		}

	public void setWidth(int w)
		{
		if (width == w) return;
		width = w;
		gridImage = null;
		}

	public void setHeight(int h)
		{
		if (height == h) return;
		height = h;
		gridImage = null;
		}

	private void paintGridTile(Graphics g, int x, int y)
		{
		if (rhombic)
			{
			int cx0 = x + (width >> 1);
			int cy0 = y + (height >> 1);
			int cx1 = x + (width + 1 >> 1);
			int cy1 = y + (height + 1 >> 1);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(GRID_DARK);
			g.drawLine(x + 1,cy1,cx0,y + height - 1);
			g.drawLine(cx1,y + height - 1,x + width - 1,cy1);
			g.drawLine(x + width,cy0 - 1,cx1 + 1,y);
			g.drawLine(cx0 - 1,y,x,cy0 - 1);
			g.setColor(GRID_BRIGHT);
			g.drawLine(x + width - 1,cy0,cx1,y + 1);
			g.drawLine(cx0,y + 1,x + 1,cy0);
			g.drawLine(x,cy1 + 1,cx0 - 1,y + height);
			g.drawLine(cx1 + 1,y + height,x + width,cy1 + 1);
			}
		else
			{
			g.setColor(GRID_DARK);
			g.drawLine(x + width,y + 1,x + width,y + height);
			g.drawLine(x + 1,y + height,x + width - 1,y + height);
			g.setColor(GRID_BRIGHT);
			g.drawLine(x + 1,y,x + 1,y + height - 1);
			g.drawLine(x + 1,y + 1,x + width - 1,y + 1);
			}
		}

	public void paint(Graphics g)
		{
		if (width < 2 || height < 2) return;
		BufferedImage gi = gridImage == null ? null : gridImage.get();
		if (gi == null && width * height <= 65536) // Limit size to 256x256 pixels or equivalent
			{
			GraphicsConfiguration gc = component.getGraphicsConfiguration();
			gi = gc == null ? new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB)
					: gc.createCompatibleImage(width,height,Transparency.TRANSLUCENT);
			Graphics2D g2 = gi.createGraphics();
			paintGridTile(g2,0,0);
			gridImage = new SoftReference<BufferedImage>(gi);
			}
		Rectangle clip = g.getClipBounds();
		int x0 = clip.x;
		int x1 = x0 + clip.width;
		int y0 = clip.y;
		int y1 = y0 + clip.height;
		if (gi != null)
			for (int y = negDiv(y0,height) * height; y <= y1; y += height)
				for (int x = negDiv(x0,width) * width; x <= x1; x += width)
					g.drawImage(gi,x,y,null);
		else
			for (int y = negDiv(y0,height) * height; y <= y1; y += height)
				for (int x = negDiv(x0,width) * width; x <= x1; x += width)
					paintGridTile(g,x,y);
		}

	private static int negDiv(int a, int b)
		{
		return a >= 0 ? a / b : ~(~a / b);
		}
	}
