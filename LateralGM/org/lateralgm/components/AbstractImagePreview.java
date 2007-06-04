/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JLabel;

public abstract class AbstractImagePreview extends JLabel
	{
	private static final long serialVersionUID = 1L;

	public AbstractImagePreview()
		{
		setOpaque(true);
		if (getImage() != null)
			{
			BufferedImage img = getImage();
			setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
			}
		else
			setPreferredSize(new Dimension(0,0));
		}

	protected abstract BufferedImage getImage();

	public void setIcon(Icon ico)
		{
		super.setIcon(ico);
		if (ico != null)
			setPreferredSize(new Dimension(ico.getIconWidth(),ico.getIconHeight()));
		else
			setPreferredSize(new Dimension(0,0));
		}

	public static void drawInvertedHorizontalLine(Graphics g, BufferedImage src, int x, int y,
			int length)
		{
		Rectangle r = g.getClipBounds().intersection(new Rectangle(x,y,length,1));
		if (!r.isEmpty() && r.x < src.getWidth() && r.y < src.getHeight())
			{
			r.width = Math.min(r.width,src.getWidth() - r.x);
			r.height = Math.min(r.height,src.getHeight() - r.y);
			BufferedImage dest = new BufferedImage(r.width,1,BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < r.width; i++)
				dest.setRGB(i,0,(~src.getRGB(r.x + i,r.y)) | 0xFF000000);
			g.drawImage(dest,r.x,r.y,null);
			}
		}

	public static void drawInvertedVerticalLine(Graphics g, BufferedImage src, int x, int y,
			int length)
		{
		Rectangle r = g.getClipBounds().intersection(new Rectangle(x,y,1,length));
		if (!r.isEmpty() && r.x < src.getWidth() && r.y < src.getHeight())
			{
			r.width = Math.min(r.width,src.getWidth() - r.x);
			r.height = Math.min(r.height,src.getHeight() - r.y);
			BufferedImage dest = new BufferedImage(1,r.height,BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < r.height; i++)
				dest.setRGB(0,i,(~src.getRGB(r.x,r.y + i)) | 0xFF000000);
			g.drawImage(dest,r.x,r.y,null);
			}
		}

	public static void drawInvertedRectangle(Graphics g, BufferedImage src, int x1, int y1, int x2,
			int y2)
		{
		int left = Math.min(x1,x2);
		int top = Math.min(y1,y2);
		int width = Math.abs(x1 - x2);
		int height = Math.abs(y1 - y2);
		drawInvertedHorizontalLine(g,src,left,top,width);
		drawInvertedHorizontalLine(g,src,left,Math.max(y1,y2),width+1);
		drawInvertedVerticalLine(g,src,left,top,height);
		drawInvertedVerticalLine(g,src,Math.max(x1,x2),top,height);
		}
	}
