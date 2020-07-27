/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.lateralgm.main.Util;
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

		if (getImage() == null) return; // << super paint already sized to 0x0
		if (!(Boolean)background.get(PBackground.USE_AS_TILESET)) return;
		int twidth = background.get(PBackground.TILE_WIDTH);
		int theight = background.get(PBackground.TILE_HEIGHT);
		twidth *= zoom;
		theight *= zoom;
		if (twidth <= 2 || theight <= 2) return; // no division by zero!
		g = g.create(); // << clone for safety
		reclipAndTranslate(g); // << intersection clip & center

		int hoffset = background.get(PBackground.H_OFFSET);
		int voffset = background.get(PBackground.V_OFFSET);
		int hsep = background.get(PBackground.H_SEP);
		int vsep = background.get(PBackground.V_SEP);

		hoffset *= zoom;
		voffset *= zoom;
		hsep *= zoom;
		vsep *= zoom;

		Dimension prefSize = getPreferredSize();
		Rectangle r = g.getClipBounds().intersection(
				new Rectangle(hoffset,voffset,prefSize.width - hoffset,prefSize.height - voffset));

		int newx = ((r.x - hoffset) / (twidth + hsep)) * (twidth + hsep) + hoffset;
		r.width += r.x - newx;
		r.x = newx;

		int newy = ((r.y - voffset) / (theight + vsep)) * (theight + vsep) + voffset;
		r.height += r.y - newy;
		r.y = newy;

		g.setXORMode(Color.BLACK);
		g.setColor(Color.WHITE);
		for (int i = r.x; i < r.x + r.width; i += twidth + hsep)
			for (int j = r.y; j < r.y + r.height; j += theight + vsep)
				g.drawRect(i,j,twidth - 1,theight - 1);

		g.dispose(); // cleanup
		}

	protected BufferedImage getImage()
		{
		if (background == null) return null;
		if (!(Boolean) background.get(PBackground.TRANSPARENT)) return background.getBackgroundImage();
		return Util.getTransparentImage(background.getBackgroundImage());
		}

	public void updated(UpdateEvent e)
		{
		// new image may be new size
		this.resizeAndRepaint();
		}

	private class BackgroundPropertyListener extends PropertyUpdateListener<PBackground>
		{
		public void updated(PropertyUpdateEvent<PBackground> e)
			{
			switch (e.key)
				{
				case PRELOAD:
				case SMOOTH_EDGES:
				case TRANSPARENT: // << handled by UpdateEvent ^^
					return;
				default:
					repaint();
				}
			}
		}
	}
