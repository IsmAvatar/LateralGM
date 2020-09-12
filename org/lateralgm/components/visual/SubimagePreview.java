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
import java.awt.Graphics;
import java.awt.Point;
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

	public UpdateListener mouseListener;

	public boolean enableMouse = true;

	public SubimagePreview(Sprite s)
		{
		super();
		sprite = s;
		s.properties.updateSource.addListener(spl);
		s.reference.updateSource.addListener(mouseListener);

		enableEvents(MouseEvent.MOUSE_PRESSED);
		enableEvents(MouseEvent.MOUSE_DRAGGED);
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);

		if (getImage() == null) return; // << super paint already sized to 0x0
		if (!showBbox && !showOrigin) return;
		g = g.create(); // << clone for safety
		reclipAndTranslate(g); // << intersection clip & center

		//TODO: The rounding that follows is extremely sensitive.
		int originX = (int) Math.floor((Integer) sprite.get(PSprite.ORIGIN_X) * zoom);
		int originY = (int) Math.floor((Integer) sprite.get(PSprite.ORIGIN_Y) * zoom);
		int left = sprite.get(PSprite.BB_LEFT);
		int right = sprite.get(PSprite.BB_RIGHT);
		int top = sprite.get(PSprite.BB_TOP);
		int bottom = sprite.get(PSprite.BB_BOTTOM);

		left = Math.min(left,right);
		right = Math.max(left,right);
		top = Math.min(top,bottom);
		bottom = Math.max(top,bottom);

		left = (int) Math.floor(left * zoom);
		top = (int) Math.floor(top * zoom);
		right = (int) Math.ceil((right + 1) * zoom) - 1;
		bottom = (int) Math.ceil((bottom + 1) * zoom) - 1;

		g.setXORMode(Color.BLACK); //XOR mode so that bbox and origin can counter
		g.setColor(Color.WHITE);
		if (showBbox) g.drawRect(left,top,right - left,bottom - top);
		if (showOrigin)
			{
			g.drawLine(originX - ORIGIN_SIZE,originY,originX + ORIGIN_SIZE,originY);
			g.drawLine(originX,originY - ORIGIN_SIZE,originX,originY + ORIGIN_SIZE);
			}

		g.dispose(); // cleanup
		}

	private void setBoundedOrigin(int x, int y)
		{
		BufferedImage img = getImage();
		int w = 0, h = 0;
		if (img != null)
			{
			w = img.getWidth();
			h = img.getHeight();
			}
		x = (int) Math.max(0,Math.min(w,x));
		y = (int) Math.max(0,Math.min(h,y));
		sprite.put(PSprite.ORIGIN_X,x);
		sprite.put(PSprite.ORIGIN_Y,y);
		}

	public void setIndex(int i)
		{
		subIndex = i;
		// we allow varying subimage size
		this.resizeAndRepaint();
		}

	public int getIndex()
		{
		return subIndex;
		}

	public void setShowBbox(boolean show)
		{
		if (show != showBbox) repaint(); // << posts event
		showBbox = show; // << repaint event happens later
		}

	public void setShowOrigin(boolean show)
		{
		if (show != showOrigin) repaint(); // << posts event
		showOrigin = show; // << repaint event happens later
		}

	protected void processMouseEvent(MouseEvent e)
		{
		if (enableMouse)
			{
			if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1)
				{
				Point pnt = this.translatePoint(e.getPoint());
				setBoundedOrigin(pnt.x,pnt.y);
				}
			}
		super.processMouseEvent(e);
		}

	protected void processMouseMotionEvent(MouseEvent e)
		{
		if (enableMouse)
			{
			if (e.getID() == MouseEvent.MOUSE_DRAGGED
					&& (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
				{
				Point pnt = this.translatePoint(e.getPoint());
				setBoundedOrigin(pnt.x,pnt.y);
				}
			}
		super.processMouseMotionEvent(e);
		}

	protected BufferedImage getImage()
		{
		if (sprite == null) return null;
		int s = sprite.subImages.size();
		if (s == 0 || subIndex < 0) return null;
		BufferedImage bi = sprite.subImages.get(subIndex % s);
		if (!(Boolean) sprite.get(PSprite.TRANSPARENT)) return bi;
		return Util.getTransparentImage(bi);
		}

	public void updated(UpdateEvent e)
		{
		// new image may be new size
		this.resizeAndRepaint();
		}

	private class SpritePropertyListener extends PropertyUpdateListener<PSprite>
		{
		public void updated(PropertyUpdateEvent<PSprite> e)
			{
			switch (e.key)
				{
				case PRELOAD:
				case SMOOTH_EDGES:
				case TRANSPARENT: // << handled by UpdateEvent ^^
				case SHAPE:
					return;
				default:
					repaint();
				}
			}
		}
	}
