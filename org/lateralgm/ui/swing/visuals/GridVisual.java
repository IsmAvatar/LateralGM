/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import static org.lateralgm.main.Util.negDiv;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.WeakHashMap;

public class GridVisual implements Visual
	{
	private static final Color GRID_DARK = new Color(0,0,0,96);
	private static final Color GRID_BRIGHT = new Color(255,255,255,96);
	private static final WeakHashMap<GraphicsConfiguration,SoftReference<LineImageData>> IMAGE_DATA;
	static
		{
		IMAGE_DATA = new WeakHashMap<GraphicsConfiguration,SoftReference<LineImageData>>(4);
		}

	private boolean rhombic;
	private int width, height;

	/*
	 * Drawing grid lines with alpha appears to be incredibly slow (at least for a 16x16 grid),
	 * so for grid sizes that aren't too large, it is drawn to an image which is then repeated over
	 * the screen.
	 */
	private BufferedImage gridImage;

	private LineImageData imageData;

	public GridVisual(boolean r, int w, int h)
		{
		rhombic = r;
		width = w;
		height = h;
		}

	public void setRhombic(boolean r)
		{
		if (r == rhombic) return;
		rhombic = r;
		flush(r);
		}

	public void setWidth(int w)
		{
		if (width == w) return;
		width = w;
		flush(false);
		}

	public void setHeight(int h)
		{
		if (height == h) return;
		height = h;
		flush(false);
		}

	public void flush(boolean full)
		{
		if (gridImage != null) gridImage.flush();
		gridImage = null;
		if (full) imageData = null;
		}

	private void paintGrid(Graphics g, int x0, int y0, int x1, int y1)
		{
		boolean rx = width >= 2;
		boolean ry = height >= 2;
		if (rhombic)
			{
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			for (int y = negDiv(y0,height) * height; y <= y1; y += height)
				for (int x = negDiv(x0,width) * width; x <= x1; x += width)
					{
					int cx0 = x + (width >> 1);
					int cy0 = y + (height >> 1);
					int cx1 = x + (width + 1 >> 1);
					int cy1 = y + (height + 1 >> 1);
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
			}
		else
			{
			GraphicsConfiguration gc = g instanceof Graphics2D ? ((Graphics2D) g).getDeviceConfiguration()
					: null;
			if (imageData == null || !imageData.configuration.equals(gc))
				{
				SoftReference<LineImageData> idr = IMAGE_DATA.get(gc);
				imageData = idr == null ? null : idr.get();
				if (imageData == null)
					{
					imageData = new LineImageData(gc);
					IMAGE_DATA.put(gc,new SoftReference<LineImageData>(imageData));
					}
				}
			if (!ry)
				for (int x = negDiv(x0,width) * width; x <= x1; x += width)
					imageData.paintVertical(g,x - 1,y0,y1 - y0);
			else if (!rx)
				for (int y = negDiv(y0,height) * height; y <= y1; y += height)
					imageData.paintHorizontal(g,x0,y - 1,x1 - x0,false);
			else
				for (int y = negDiv(y0,height) * height; y <= y1; y += height)
					for (int x = negDiv(x0,width) * width; x <= x1; x += width)
						{
						imageData.paintHorizontal(g,x - 1,y - 1,Math.min(width,x1 - x + 1),true);
						imageData.paintVertical(g,x - 1,y + 1,Math.min(height - 2,y1 - y - 1));
						}
			}
		}

	public void paint(Graphics g)
		{
		boolean rx = width >= 2;
		boolean ry = height >= 2;
		if (!rx && !ry || (rhombic && (!rx || !ry))) return;
		int iw = rx ? width * ((48 + width - 1) / width) : 64;
		int ih = ry ? height * ((48 + height - 1) / height) : 64;
		if (gridImage == null
				&& (rx && ry ? (width * height <= (rhombic ? 65536 : 9216)) : (rx ? width : height) < 16))
			{
			GraphicsConfiguration gc = g instanceof Graphics2D ? ((Graphics2D) g).getDeviceConfiguration()
					: null;
			gridImage = gc == null ? new BufferedImage(iw,ih,BufferedImage.TYPE_INT_ARGB)
					: gc.createCompatibleImage(iw,ih,Transparency.TRANSLUCENT);
			Graphics2D g2 = gridImage.createGraphics();
			paintGrid(g2,0,0,iw,ih);
			}
		Rectangle clip = g.getClipBounds();
		int x0 = clip.x;
		int x1 = x0 + clip.width;
		int y0 = clip.y;
		int y1 = y0 + clip.height;
		if (gridImage != null)
			for (int y = negDiv(y0,ih) * ih; y <= y1; y += ih)
				for (int x = negDiv(x0,iw) * iw; x <= x1; x += iw)
					g.drawImage(gridImage,x,y,null);
		else
			paintGrid(g,x0,y0,x1,y1);
		}

	class LineImageData
		{
		private final BufferedImage horizSub, horizontal, vertical;
		public final GraphicsConfiguration configuration;

		public LineImageData(GraphicsConfiguration gc)
			{
			configuration = gc;
			if (gc != null)
				{
				horizontal = gc.createCompatibleImage(130,2,Transparency.TRANSLUCENT);
				vertical = gc.createCompatibleImage(2,128,Transparency.TRANSLUCENT);
				}
			else
				{
				horizontal = new BufferedImage(130,2,BufferedImage.TYPE_INT_ARGB);
				vertical = new BufferedImage(2,128,BufferedImage.TYPE_INT_ARGB);
				}
			int[] rgba = new int[128];
			Arrays.fill(rgba,GRID_DARK.getRGB());
			horizontal.setRGB(0,0,rgba[0]);
			horizontal.setRGB(2,0,128,1,rgba,0,128);
			vertical.setRGB(0,0,1,128,rgba,0,1);
			Arrays.fill(rgba,GRID_BRIGHT.getRGB());
			horizontal.setRGB(1,1,rgba[0]);
			horizontal.setRGB(2,1,128,1,rgba,0,128);
			vertical.setRGB(1,0,1,128,rgba,0,1);
			horizSub = horizontal.getSubimage(2,0,128,2);
			}

		public void paintHorizontal(Graphics g, int x, int y, int l, boolean start)
			{
			if (start)
				{
				if (l >= 130)
					g.drawImage(horizontal,x,y,null);
				else
					g.drawImage(horizontal,x,y,x + l,y + 2,0,0,l,2,null);
				}
			int t;
			for (t = start ? 130 : 0; t <= l - 128; t += 128)
				g.drawImage(horizSub,x + t,y,null);
			if (t < l) g.drawImage(horizSub,x + t,y,x + l,y + 2,0,0,l - t,2,null);
			}

		public void paintVertical(Graphics g, int x, int y, int l)
			{
			int t;
			for (t = 0; t <= l - 128; t += 128)
				g.drawImage(vertical,x,y + t,null);
			if (t < l) g.drawImage(vertical,x,y + t,x + 2,y + l,0,0,2,l - t,null);
			}
		}
	}
