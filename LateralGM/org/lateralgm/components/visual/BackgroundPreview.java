/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.lateralgm.subframes.BackgroundFrame;

public class BackgroundPreview extends AbstractImagePreview
	{
	private static final long serialVersionUID = 1L;

	private BackgroundFrame frame;

	public BackgroundPreview(BackgroundFrame frame)
		{
		super();
		this.frame = frame;
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		if (frame.tileset.isSelected())
			{
			BufferedImage img = getImage();
			if (img != null)
				{
				int width = frame.tWidth.getIntValue();
				int height = frame.tHeight.getIntValue();
				if (width > 2 && height > 2)
					{
					int hoffset = frame.hOffset.getIntValue();
					int voffset = frame.vOffset.getIntValue();
					int hsep = frame.hSep.getIntValue();
					int vsep = frame.vSep.getIntValue();

					Rectangle r = g.getClipBounds().intersection(
							new Rectangle(hoffset,voffset,img.getWidth() - hoffset,img.getHeight() - voffset));

					int newx = ((r.x - hoffset) / (width + hsep)) * (width + hsep) + hoffset;
					r.width += r.x - newx;
					r.x = newx;

					int newy = ((r.y - voffset) / (height + vsep)) * (height + vsep) + voffset;
					r.height += r.y - newy;
					r.y = newy;

					//Thanks to javaman1922 for figuring this trick out
					Image dbImage = createImage(img.getWidth(),img.getHeight());
					Graphics g2 = dbImage.getGraphics();
					g2.setColor(Color.BLACK); //necessary to prevent resolution loss
					g2.fillRect(r.x,r.y,r.x + r.width,r.y + r.height);
					g2.setColor(Color.WHITE);
					for (int i = r.x; i < r.x + r.width; i += width + hsep)
						for (int j = r.y; j < r.y + r.height; j += height + vsep)
							g2.drawRect(i,j,width - 1,height - 1);
					g.setXORMode(Color.BLACK);
					g.drawImage(dbImage,0,0,null);
					g.setPaintMode();
					}
				}
			}
		}

	protected BufferedImage getImage()
		{
		if (frame != null) return frame.res.backgroundImage;
		return null;
		}
	}
