/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.lateralgm.subframes.SpriteFrame;

public class SubimagePreview extends AbstractImagePreview
	{
	private static final long serialVersionUID = 1L;

	private SpriteFrame frame;

	private static final int ORIGIN_SIZE = 20;

	public SubimagePreview(SpriteFrame frame)
		{
		super();
		this.frame = frame;
		enableEvents(MouseEvent.MOUSE_PRESSED);
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		BufferedImage img = frame.getSubimage();
		if (img != null)
			{
			setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
			int originX = frame.originX.getIntValue();
			int originY = frame.originY.getIntValue();
			int bboxLeft = frame.bboxLeft.getIntValue();
			int bboxRight = frame.bboxRight.getIntValue();
			int bboxTop = frame.bboxTop.getIntValue();
			int bboxBottom = frame.bboxBottom.getIntValue();

			int left = Math.min(bboxLeft,bboxRight);
			int right = Math.max(bboxLeft,bboxRight);
			int top = Math.min(bboxTop,bboxBottom);
			int bottom = Math.max(bboxTop,bboxBottom);

			Shape oldClip = g.getClip(); //backup the old clip
			Rectangle oldc = g.getClipBounds();
			//Set the clip properly
			g.setClip(new Rectangle(oldc.x,oldc.y,Math.min(oldc.x + oldc.width,img.getWidth()) - oldc.x,
					Math.min(oldc.y + oldc.height,img.getHeight()) - oldc.y));

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

	protected void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1
				&& e.getX() < getPreferredSize().width && e.getY() < getPreferredSize().height)
			{
			frame.originX.setIntValue(e.getX());
			frame.originY.setIntValue(e.getY());
			}
		super.processMouseEvent(e);
		}

	protected BufferedImage getImage()
		{
		if (frame != null) return frame.getSubimage();
		return null;
		}
	}
