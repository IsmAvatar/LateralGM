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
		g2.dispose();
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
		private int x, y, width, height;
		private boolean doListen;

		public InstanceComponent(Instance i)
			{
			instance = i;
			object = deRef(i.gmObjectId);
			rcl = new ResourceChangeListener();
			if (object == null)
				image = EMPTY_IMAGE;
			}

		private void setListen(boolean l)
			{
			if (l == doListen) return;
			if (l)
				{
				if (sprite != null) sprite.addChangeListener(rcl);
				if (object != null) object.addChangeListener(rcl);
				}
			else
				{
				if (sprite != null) sprite.removeChangeListener(rcl);
				if (object != null) object.removeChangeListener(rcl);
				}
			doListen = l;
			}

		private void updateSprite()
			{
			Sprite s = deRef(object.sprite);
			if (s != sprite)
				{
				if (sprite != null) sprite.removeChangeListener(rcl);
				if (doListen && s != null) s.addChangeListener(rcl);
				image = null;
				sprite = s;
				}
			}

		private void updateBounds()
			{
			x = instance.x - (sprite == null ? 0 : sprite.originX);
			y = instance.y - (sprite == null ? 0 : sprite.originY);
			if (sprite == null)
				{
				width = EMPTY_IMAGE.getWidth();
				height = EMPTY_IMAGE.getHeight();
				}
			else
				{
				width = sprite.width;
				height = sprite.height;
				}
			}

		private void updateImage()
			{
			image = sprite == null ? null : sprite.getDisplayImage();
			if (image == null)
				{
				image = EMPTY_IMAGE;
				setOpaque(false);
				}
			else
				{
				setOpaque(!sprite.transparent);
				}
			}

		public void paintComponent(Graphics g)
			{
			if (object == null)
				{
				getParent().remove(this);
				return;
				}
			if (image == null) updateImage();
			g.drawImage(image,0,0,null);
			}

		@Override
		public int getHeight()
			{
			return height;
			}

		@Override
		public int getWidth()
			{
			return width;
			}

		@Override
		public int getX()
			{
			return x;
			}

		@Override
		public int getY()
			{
			return y;
			}

		@Override
		public void addNotify()
			{
			super.addNotify();
			updateSprite();
			updateBounds();
			setListen(true);
			}

		@Override
		public void removeNotify()
			{
			super.removeNotify();
			setListen(false);
			}

		private class ResourceChangeListener implements ChangeListener
			{
			public void stateChanged(ChangeEvent e)
				{
				updateSprite();
				updateBounds();
				repaint();
				}
			}
		}
	}
