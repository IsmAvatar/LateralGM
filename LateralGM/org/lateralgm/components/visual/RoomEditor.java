/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.subframes.RoomFrame;

public class RoomEditor extends JPanel implements ImageObserver
	{
	private static final long serialVersionUID = 1L;

	private RoomFrame frame;

	public RoomEditor(RoomFrame frame)
		{
		setOpaque(false);
		this.frame = frame;
		enableEvents(MouseEvent.MOUSE_PRESSED);
		}

	//TODO
	protected void processMouseEvent(MouseEvent e)
		{
		super.processMouseEvent(e);
		if (e.getX() >= getPreferredSize().width && e.getY() >= getPreferredSize().height) return;
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1)
			{
			return;
			//check delete underlying
			//add object/tile
			}
		}

	//TODO: This is unfinished
	@Override
	public void paintComponent(Graphics g)
		{
		int width = frame.sWidth.getIntValue();
		int height = frame.sHeight.getIntValue();
		g.setColor(frame.bDrawColor.isSelected() ? frame.bColor.getSelectedColor() : Color.BLACK);
		g.fillRect(0,0,width,height);
		if (frame.bVisible.isSelected() && frame.sSBack.isSelected())
			{
			for (int i = 0; i < 8; i++)
				{
				BackgroundDef bd = frame.res.backgroundDefs[i];
				if (!bd.visible || bd.foreground) continue;
				BufferedImage bi = bd.backgroundId.getRes().backgroundImage;
				if (bd.stretch)
					g.drawImage(bi,bd.x,bd.y,width,height,this);
				else
					g.drawImage(bi,bd.x,bd.y,this);
				}
			}
		if (frame.sSTile.isSelected())
			{
			for (Tile t : frame.res.tiles)
				{
				BufferedImage bi = t.backgroundId.getRes().backgroundImage;
				g.drawImage(bi.getSubimage(t.tileX,t.tileY,t.width,t.height),t.x,t.y,this);
				}
			}
		if (frame.sGridVis.isSelected())
			{
			int w = frame.sSnapX.getIntValue();
			int h = frame.sSnapY.getIntValue();
			if (w > 3)
				{
				for (int x = 0; x < width; x += w)
					{
					g.setXORMode(Color.WHITE);
					g.setColor(Color.BLACK);
					g.drawLine(x,0,x,height);
					}
				}
			if (h > 3)
				{
				for (int y = 0; y < height; y += h)
					{
					g.setXORMode(Color.WHITE);
					g.setColor(Color.BLACK);
					g.drawLine(0,y,width,y);
					}
				}
			}
		}
	}
