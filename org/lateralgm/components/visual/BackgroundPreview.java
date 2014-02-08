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
import java.awt.Graphics2D;
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

	private double zoom = 1.0;
	private BufferedImage transparentBackground = null;

	private Object transparentImage;
	
	public BackgroundPreview(Background b)
		{
		super();
		background = b;
		b.properties.updateSource.addListener(bpl);
		b.reference.updateSource.addListener(this);
		}
	
	protected BufferedImage getTransparentImage()
		{
		if (background == null) return null;
		if (!(Boolean) background.get(PBackground.TRANSPARENT)) return background.getBackgroundImage();
		return Util.getTransparentIcon(background.getBackgroundImage());
		}
	
	public BufferedImage paintBackground() {
		BufferedImage img = getImage();
		BufferedImage dest = new BufferedImage(
				img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dest.createGraphics();
  
		int imgwidth = img.getWidth();
		int imgheight = img.getHeight();

		g.setClip(0,0,imgwidth,imgheight);
		int TILE = 5;
		g.setColor(Color.lightGray);
		int w = imgwidth / TILE + 1;
		int h = imgheight / TILE + 1;
		for (int row = 0; row < h; row++) {
  			for (int col = 0; col < w; col++) {
      			if ((row + col) % 2 == 0) {
          			g.fillRect(col * TILE, row * TILE, TILE, TILE);
      			}
  			}
		}
		return dest;
	}

	public void paintComponent(Graphics g)
		{
		//super.paintComponent(g);
		g.setColor(this.getBackground());
		g.fillRect(0,0,this.getWidth(),this.getHeight());
		
		
		BufferedImage image = getImage();
		
		if ((Boolean) background.get(PBackground.TRANSPARENT)) {
			if (transparentImage == null) {
				image = getTransparentImage();
			}
		} else {
			image = getImage();
			transparentImage = null;
		}
		
		
		Dimension d = getPreferredSize();
		
		if (image != null) {
			if (transparentBackground == null) {
				transparentBackground = paintBackground();
			}
			
	    Graphics2D g2d = (Graphics2D) g;
	    g2d.translate(this.getWidth()/2 - d.width/2, 
	    		this.getHeight()/2 - d.height/2);
	    
			g.drawImage(transparentBackground, 0, 0, d.width, d.height, null);
    
			g.drawImage(image, 0, 0, d.width, d.height, null);
		}		else
			setPreferredSize(new Dimension(0,0));
		
		if (background.get(PBackground.USE_AS_TILESET))
			{
			if (image != null)
				{
				int width = background.get(PBackground.TILE_WIDTH);
				int height = background.get(PBackground.TILE_HEIGHT);
				if (width > 2 && height > 2)
					{
					int hoffset = background.get(PBackground.H_OFFSET);
					int voffset = background.get(PBackground.V_OFFSET);
					int hsep = background.get(PBackground.H_SEP);
					int vsep = background.get(PBackground.V_SEP);

					width *= zoom;
					height *= zoom;
					hoffset *= zoom;
					voffset *= zoom;
					hsep *= zoom;
					vsep *= zoom;

					Rectangle r = g.getClipBounds().intersection(
							new Rectangle(hoffset,voffset,d.width - hoffset,d.height - voffset));

					int newx = ((r.x - hoffset) / (width + hsep)) * (width + hsep) + hoffset;
					r.width += r.x - newx;
					r.x = newx;

					int newy = ((r.y - voffset) / (height + vsep)) * (height + vsep) + voffset;
					r.height += r.y - newy;
					r.y = newy;

					g.setClip(0, 0, d.width, d.height); 
					g.setXORMode(Color.BLACK);
					g.setColor(Color.WHITE);
					for (int i = r.x; i < r.x + r.width; i += width + hsep)
						for (int j = r.y; j < r.y + r.height; j += height + vsep)
							g.drawRect(i,j,width - 1,height - 1);
					g.setPaintMode(); //just in case
					}
				}
			}
		}
	
	public Dimension getPreferredSize()
		{
		BufferedImage img = getImage();
		if (img == null) return super.getPreferredSize();
		return new Dimension((int)(img.getWidth()*zoom),(int)(img.getHeight()*zoom));
		}

	public double getZoom() {
		return zoom;
	}
	
	public void setZoom(double nzoom) {
		zoom = nzoom;
		updateUI();
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
