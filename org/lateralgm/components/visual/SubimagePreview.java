/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Sprite.PSprite;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public class SubimagePreview extends AbstractImagePreview implements UpdateListener
	{
	private static final long serialVersionUID = 1L;

	private int subIndex = 0;
	private boolean showBbox = true;

	private final Sprite sprite;
	private final SpritePropertyListener spl = new SpritePropertyListener();

	private static final int ORIGIN_SIZE = 20;

	public SubimagePreview(Sprite s)
		{
		super();
		sprite = s;
		s.properties.updateSource.addListener(spl);
		s.reference.updateSource.addListener(this);
		enableEvents(MouseEvent.MOUSE_PRESSED);
		enableEvents(MouseEvent.MOUSE_DRAGGED);
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		BufferedImage img = getImage();
		if (img != null && showBbox)
			{
			setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
			int originX = sprite.get(PSprite.ORIGIN_X);
			int originY = sprite.get(PSprite.ORIGIN_Y);
			int bboxLeft = sprite.get(PSprite.BB_LEFT);
			int bboxRight = sprite.get(PSprite.BB_RIGHT);
			int bboxTop = sprite.get(PSprite.BB_TOP);
			int bboxBottom = sprite.get(PSprite.BB_BOTTOM);

			int left = Math.min(bboxLeft,bboxRight);
			int right = Math.max(bboxLeft,bboxRight);
			int top = Math.min(bboxTop,bboxBottom);
			int bottom = Math.max(bboxTop,bboxBottom);

			Shape oldClip = reclip(g);

			g.setXORMode(Color.BLACK); //XOR mode so that bbox and origin can counter
			g.setColor(Color.WHITE);

			g.drawRect(left,top,right - left,bottom - top);
			g.drawLine(originX - ORIGIN_SIZE,originY,originX + ORIGIN_SIZE,originY);
			g.drawLine(originX,originY - ORIGIN_SIZE,originX,originY + ORIGIN_SIZE);

			g.setPaintMode(); //just in case
			g.setClip(oldClip); //restore the clip
			}
		else
			setPreferredSize(new Dimension(0,0));
		}

	private void setBoundedOrigin(int x, int y)
		{
		Dimension d = getPreferredSize();
		x = Math.max(0,Math.min(d.width - 1,x));
		y = Math.max(0,Math.min(d.height - 1,y));
		sprite.put(PSprite.ORIGIN_X,x);
		sprite.put(PSprite.ORIGIN_Y,y);
		}

	public void setIndex(int i)
		{
		subIndex = i;
		updateUI();
		}

	public void setShowBbox(boolean show)
		{
		showBbox = show;
		}

	protected void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1
				&& e.getX() < getPreferredSize().width && e.getY() < getPreferredSize().height)
			setBoundedOrigin(e.getX(),e.getY());
		super.processMouseEvent(e);
		}

	protected void processMouseMotionEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_DRAGGED && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
			setBoundedOrigin(e.getX(),e.getY());
		super.processMouseMotionEvent(e);
		}

	protected BufferedImage getImage()
		{
		if (sprite == null) return null;
		int s = sprite.subImages.size();
		return s == 0 || subIndex < 0 ? null : sprite.subImages.get(subIndex % s);
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
