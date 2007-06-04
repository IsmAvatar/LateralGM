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

			drawInvertedHorizontalLine(g,img,originX - ORIGIN_SIZE,originY,2 * ORIGIN_SIZE);
			drawInvertedVerticalLine(g,img,originX,originY - ORIGIN_SIZE,2 * ORIGIN_SIZE);
			drawInvertedRectangle(g,img,bboxLeft,bboxTop,bboxRight,bboxBottom);
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
