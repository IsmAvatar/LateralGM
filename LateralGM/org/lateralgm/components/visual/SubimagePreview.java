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
import java.awt.Image;
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

			//Thanks to javaman1922 for figuring this trick out.
			//In order to keep drawing in-bounds, we draw on an Image first,
			Image dbImage = createImage(img.getWidth(),img.getHeight());
			Graphics g2 = dbImage.getGraphics();
			g2.setPaintMode();
			g2.setColor(Color.BLACK); //Must fill background first, or image loses quality
			g2.fillRect(0,0,img.getWidth(),img.getHeight());
			g2.setXORMode(Color.BLACK); //XOR mode so that bbox and origin can counter
			g2.setColor(Color.WHITE);

			g2.drawRect(Math.min(bboxLeft,bboxRight),Math.min(bboxTop,bboxBottom),Math.max(bboxLeft,
					bboxRight),Math.max(bboxTop,bboxBottom));
			g2.drawLine(originX - ORIGIN_SIZE,originY,originX + ORIGIN_SIZE,originY);
			g2.drawLine(originX,originY - ORIGIN_SIZE,originX,originY + ORIGIN_SIZE);

			//Now draw our in-bounds image back on the original Graphics
			g.setXORMode(Color.BLACK);
			g.setColor(Color.WHITE);
			g.drawImage(dbImage,0,0,null);
			g.setPaintMode();
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
