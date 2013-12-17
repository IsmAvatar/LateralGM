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
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
		return new Dimension((int)(img.getWidth()*zoom),(int)(img.getHeight()*zoom));
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
		
		int imgwidth = 0;
		int imgheight = 0;
		
		if (image != null) {
			imgwidth = (int)(image.getWidth()*zoom);
			imgheight = (int)(image.getHeight()*zoom);

			if (transparentBackground == null) {
				transparentBackground = paintBackground();
			}
			
			g.drawImage(transparentBackground, 0, 0, imgwidth, imgheight, null);
    
			g.drawImage(image, 0, 0, imgwidth, imgheight, null);
		}		else
			setPreferredSize(new Dimension(0,0));

		if (image != null && (showBbox || showOrigin))
			{
			int originX = sprite.get(PSprite.ORIGIN_X);
			originX *= zoom;
			int originY = sprite.get(PSprite.ORIGIN_Y);
			originY *= zoom;
			int bboxLeft = sprite.get(PSprite.BB_LEFT);
			bboxLeft *= zoom;
			int bboxRight = sprite.get(PSprite.BB_RIGHT);
			bboxRight *= zoom;
			int bboxTop = sprite.get(PSprite.BB_TOP);
			bboxTop *= zoom;
			int bboxBottom = sprite.get(PSprite.BB_BOTTOM);
			bboxBottom *= zoom;
			
			int left = Math.min(bboxLeft,bboxRight);
			int right = Math.max(bboxLeft,bboxRight);
			int top = Math.min(bboxTop,bboxBottom);
			int bottom = Math.max(bboxTop,bboxBottom);

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
		x = (int) Math.max(0,Math.min(d.width/zoom - 1,x));
		y = (int) Math.max(0,Math.min(d.height/zoom - 1,y));
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
		int mx = (int)(e.getX()/zoom);
		int my = (int)(e.getY()/zoom);
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1
				&& mx < getPreferredSize().width && my < getPreferredSize().height)
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
