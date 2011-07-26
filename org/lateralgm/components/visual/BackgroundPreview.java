/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Background.PBackground;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class BackgroundPreview extends AbstractImagePreview implements UpdateListener
	{
	private static final long serialVersionUID = 1L;

	private final Background background;
	private final BackgroundPropertyListener bpl = new BackgroundPropertyListener();

	public BackgroundPreview(Background b)
		{
		super();
		background = b;
		b.properties.updateSource.addListener(bpl);
		b.reference.updateSource.addListener(this);
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		if (background.get(PBackground.USE_AS_TILESET))
			{
			BufferedImage img = getImage();
			if (img != null)
				{
				int width = background.get(PBackground.TILE_WIDTH);
				int height = background.get(PBackground.TILE_HEIGHT);
				if (width > 2 && height > 2)
					{
					int hoffset = background.get(PBackground.H_OFFSET);
					int voffset = background.get(PBackground.V_OFFSET);
					int hsep = background.get(PBackground.H_SEP);
					int vsep = background.get(PBackground.V_SEP);

					Shape oldClip = reclip(g);

					Rectangle r = g.getClipBounds().intersection(
							new Rectangle(hoffset,voffset,img.getWidth() - hoffset,img.getHeight() - voffset));

					int newx = ((r.x - hoffset) / (width + hsep)) * (width + hsep) + hoffset;
					r.width += r.x - newx;
					r.x = newx;

					int newy = ((r.y - voffset) / (height + vsep)) * (height + vsep) + voffset;
					r.height += r.y - newy;
					r.y = newy;

					g.setXORMode(Color.BLACK);
					g.setColor(Color.WHITE);
					for (int i = r.x; i < r.x + r.width; i += width + hsep)
						for (int j = r.y; j < r.y + r.height; j += height + vsep)
							g.drawRect(i,j,width - 1,height - 1);
					g.setPaintMode(); //just in case
					g.setClip(oldClip); //restore the clip
					}
				}
			}
		}

	protected BufferedImage getImage()
		{
		return background == null ? null : background.getBackgroundImage();
		}

	public void updated(UpdateEvent e)
		{
		updateUI();
		}

	private class BackgroundPropertyListener extends PropertyUpdateListener<PBackground>
		{
		public void updated(PropertyUpdateEvent<PBackground> e)
			{
			switch (e.key)
				{
				case PRELOAD:
				case SMOOTH_EDGES:
				case TRANSPARENT:
					return;
				default:
					repaint();
				}
			}
		}
	}
