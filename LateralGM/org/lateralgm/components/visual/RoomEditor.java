/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import static org.lateralgm.resources.Ref.deRef;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.main.Util;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.subframes.RoomFrame;

public class RoomEditor extends JPanel implements ImageObserver
	{
	private static final long serialVersionUID = 1L;

	private RoomFrame frame;

	public RoomEditor(Room r, RoomFrame frame)
		{
		setOpaque(false);
		this.frame = frame;
		enableEvents(MouseEvent.MOUSE_PRESSED);
		for (Instance i : r.instances)
			add(new RoomEditor.InstanceComponent(i));
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
		Graphics g2 = g.create();
		int width = frame.sWidth.getIntValue();
		int height = frame.sHeight.getIntValue();
		g2.setColor(frame.bDrawColor.isSelected() ? frame.bColor.getSelectedColor() : Color.BLACK);
		g2.fillRect(0,0,width,height);
		if (frame.bVisible.isSelected() && frame.sSBack.isSelected())
			{
			for (int i = 0; i < 8; i++)
				{
				BackgroundDef bd = frame.res.backgroundDefs[i];
				if (!bd.visible || bd.foreground || deRef(bd.backgroundId) == null) continue;
				BufferedImage bi = bd.backgroundId.getRes().backgroundImage;
				if (bd.stretch)
					g2.drawImage(bi,bd.x,bd.y,width,height,this);
				else
					g2.drawImage(bi,bd.x,bd.y,this);
				}
			}
		if (frame.sSTile.isSelected())
			{
			for (Tile t : frame.res.tiles)
				{
				BufferedImage bi = t.backgroundId.getRes().backgroundImage;
				g2.drawImage(bi.getSubimage(t.tileX,t.tileY,t.width,t.height),t.x,t.y,this);
				}
			}
		if (frame.sGridVis.isSelected())
			{
			int w = frame.sSnapX.getIntValue();
			int h = frame.sSnapY.getIntValue();
			if (w > 3)
				{
				g2.setXORMode(Color.BLACK);
				g2.setColor(Color.WHITE);
				for (int x = 0; x < width; x += w)
					g2.drawLine(x,0,x,height);
				}
			if (h > 3)
				{
				g2.setXORMode(Color.BLACK);
				g2.setColor(Color.WHITE);
				for (int y = 0; y < height; y += h)
					g2.drawLine(0,y,width,y);
				}
			}
		}

	public static class InstanceComponent extends JComponent
		{
		private static final long serialVersionUID = 1L;
		private static final BufferedImage EMPTY_IMAGE = new BufferedImage(16,16,
				BufferedImage.TYPE_INT_ARGB);
		private final Instance instance;
		private final GmObject object;
		private Sprite sprite;
		private BufferedImage image;
		private final ResourceChangeListener rcl;

		public InstanceComponent(Instance i)
			{
			instance = i;
			object = deRef(i.gmObjectId);
			rcl = new ResourceChangeListener();
			if (object == null)
				{
				sprite = null;
				image = EMPTY_IMAGE;
				}
			else
				{
				object.addChangeListener(rcl);
				updateImage();
				}
			}

		private void updateImage()
			{
			if (sprite != null)
				sprite.removeChangeListener(rcl);
			sprite = deRef(object.sprite);
			if (sprite == null || sprite.subImages.size() < 1)
				{
				image = EMPTY_IMAGE;
				setOpaque(false);
				}
			else if (!sprite.subImages.get(0).equals(image))
				{
				sprite.addChangeListener(rcl);
				image = sprite.subImages.get(0);
				setOpaque(!sprite.transparent);
				if (sprite.transparent) image = Util.getTransparentIcon(image);
				}
			}

		public void paintComponent(Graphics g)
			{
			if (object == null)
				{
				getParent().remove(this);
				return;
				}
			g.drawImage(image,0,0,null);
			}

		@Override
		public int getHeight()
			{
			return image.getHeight();
			}

		@Override
		public int getWidth()
			{
			return image.getWidth();
			}

		@Override
		public int getX()
			{
			return instance.x;
			}

		@Override
		public int getY()
			{
			return instance.y;
			}

		private class ResourceChangeListener implements ChangeListener
			{
			public void stateChanged(ChangeEvent e)
				{
				updateImage();
				repaint();
				}
			}
		}
	}
