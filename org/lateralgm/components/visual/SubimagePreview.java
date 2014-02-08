/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class SubimagePreview extends AbstractImagePreview implements UpdateListener
	{
	private static final long serialVersionUID = 1L;

	private int subIndex = 0;
	private boolean showBbox = true, showOrigin = true;

	private final Sprite sprite;
	private final SpritePropertyListener spl = new SpritePropertyListener();

	private static final int ORIGIN_SIZE = 20;
	
	private double zoom = 1;
	
	private BufferedImage transparentBackground;
	private BufferedImage transparentImage;
	private BufferedImage image;
	
	public UpdateListener mouseListener;
	
	public boolean enablemouse = true;

	public SubimagePreview(Sprite s)
		{
		super();
		sprite = s;
		s.properties.updateSource.addListener(spl);
		s.reference.updateSource.addListener(mouseListener);
		
		enableEvents(MouseEvent.MOUSE_PRESSED);
		enableEvents(MouseEvent.MOUSE_DRAGGED);
		}

	public Dimension getPreferredSize()
		{
		BufferedImage img = getImage();
		if (img == null) return super.getPreferredSize();
		return new Dimension((int)Math.ceil(img.getWidth()*zoom),(int)Math.ceil(img.getHeight()*zoom));
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
		if ((Boolean) sprite.get(PSprite.TRANSPARENT)) {
			if (transparentImage == null) {
				image = getTransparentImage();
			}
		} else {
			image = getImage();
			transparentImage = null;
		}
		
		//super.paintComponent(g);		
		g.setColor(this.getBackground());
		g.fillRect(0,0,this.getWidth(),this.getHeight());
		
		if (image != null) {
			Dimension d = getPreferredSize();

			if (transparentBackground == null) {
				transparentBackground = paintBackground();
			}
			
	    Graphics2D g2d = (Graphics2D) g;
	    g2d.translate(this.getWidth()/2 - d.width/2, 
	    		this.getHeight()/2 - d.height/2);
			
			g.drawImage(transparentBackground, 0, 0, d.width, d.height, null);
    
			g.drawImage(image, 0, 0, d.width, d.height, null);
		}	else {
			setPreferredSize(new Dimension(0,0));
		}

		if (image != null && (showBbox || showOrigin))
			{
			//TODO: The rounding that follows is extremely sensitive.
			int originX = (int) Math.floor((Integer)sprite.get(PSprite.ORIGIN_X) * zoom);
			int originY = (int) Math.floor((Integer)sprite.get(PSprite.ORIGIN_Y) * zoom);
			int left = sprite.get(PSprite.BB_LEFT);
			int right = sprite.get(PSprite.BB_RIGHT);
			int top = sprite.get(PSprite.BB_TOP);
			int bottom = sprite.get(PSprite.BB_BOTTOM);
			
			left = Math.min(left,right);
			right = Math.max(left,right);
			top = Math.min(top,bottom);
			bottom = Math.max(top,bottom);

			left = (int)Math.floor(left * zoom);
			top = (int)Math.floor(top * zoom);
			right = (int)Math.ceil((right + 1) * zoom) - 1;
			bottom = (int)Math.ceil((bottom + 1) * zoom) - 1;

			//Shape oldClip = reclip(g);

			g.setXORMode(Color.BLACK); //XOR mode so that bbox and origin can counter
			g.setColor(Color.WHITE);
			if (showBbox) g.drawRect(left,top,right - left,bottom - top);
			if (showOrigin)
			{
				g.drawLine(originX - ORIGIN_SIZE,originY,originX + ORIGIN_SIZE,originY);
				g.drawLine(originX,originY - ORIGIN_SIZE,originX,originY + ORIGIN_SIZE);
			}

			g.setPaintMode(); //just in case
			//g.setClip(oldClip); //restore the clip
			}
		
		g.dispose();
		}
	
	private void setBoundedOrigin(int x, int y)
		{
		Dimension d = getPreferredSize();
		x = (int) Math.max(0,Math.min(getImage().getWidth(),x));
		y = (int) Math.max(0,Math.min(getImage().getHeight(),y));
		sprite.put(PSprite.ORIGIN_X,x);
		sprite.put(PSprite.ORIGIN_Y,y);
		}

	public double getZoom() {
		return zoom;
	}
	
	public void setZoom(double nzoom) {
		zoom = nzoom;
		updateUI();
	}
	
	public void setIndex(int i)
		{
		subIndex = i;
		updateUI();
		this.repaint();
		}
	
	public int getIndex()
		{
		return subIndex;
		}

	public void setShowBbox(boolean show)
		{
		showBbox = show;
		}

	public void setShowOrigin(boolean show)
		{
		showOrigin = show;
		}

	protected void processMouseEvent(MouseEvent e)
	{
		if (enablemouse) {
		int mx = (int)Math.floor(e.getX()/zoom);
		int my = (int)Math.floor(e.getY()/zoom);
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1)
			setBoundedOrigin(mx,my);
		}
		super.processMouseEvent(e);
	}

	protected void processMouseMotionEvent(MouseEvent e)
		{
		if (enablemouse) {
		if (e.getID() == MouseEvent.MOUSE_DRAGGED && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
			setBoundedOrigin((int)(e.getX()/zoom),(int)(e.getY()/zoom));
		}
		super.processMouseMotionEvent(e);
		}

	protected BufferedImage getImage()
		{
		if (sprite == null) return null;
		int s = sprite.subImages.size();
		if (s == 0 || subIndex < 0) return null;
		BufferedImage bi = sprite.subImages.get(subIndex % s);
		return bi;
		}
	
	protected BufferedImage getTransparentImage()
		{
		if (sprite == null) return null;
		int s = sprite.subImages.size();
		if (s == 0 || subIndex < 0) return null;
		BufferedImage bi = sprite.subImages.get(subIndex % s);
		if (!(Boolean) sprite.get(PSprite.TRANSPARENT)) return bi;
		return Util.getTransparentIcon(bi);
		}


	public void updated(UpdateEvent e)
		{
		updateUI();
		}

	private class SpritePropertyListener extends PropertyUpdateListener<PSprite>
		{
		public void updated(PropertyUpdateEvent<PSprite> e)
			{
			switch (e.key)
				{
				case PRELOAD:
				case SMOOTH_EDGES:
				case TRANSPARENT:
				case SHAPE:
					return;
				default:
					repaint();
				}
			}
		}
	}
