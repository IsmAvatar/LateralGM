/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 IsmAvatar <IsmAvatar@gmail.com>
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.lateralgm.components.impl.SpriteStripDialog;

public class SpriteStripPreview extends AbstractImagePreview implements PropertyChangeListener
	{
	private static final long serialVersionUID = 1L;

	private final SpriteStripDialog props;

	public SpriteStripPreview(SpriteStripDialog sd)
		{
		super();
		props = sd;
		setImage(getImage());
		//		sd.addListener(spl);
		enableEvents(MouseEvent.MOUSE_PRESSED);
		enableEvents(MouseEvent.MOUSE_DRAGGED);
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		BufferedImage img = getImage();
		if (img != null)
			{
			Shape oldClip = reclip(g);

			g.setXORMode(Color.BLACK); //XOR mode so that bbox and origin can counter
			g.setColor(Color.WHITE);

			/* IMAGE_NUMBER = 0, IMAGES_PER_ROW = 1, CELL_WIDTH = 2, CELL_HEIGHT = 3,
			HOR_CELL_OFFSET = 4, VERT_CELL_OFFSET = 5, HOR_PIXEL_OFFSET = 6, VERT_PIXEL_OFFSET = 7,
			HOR_SEP = 8, VERT_SEP = 9;*/

			int cw = props.fields[SpriteStripDialog.CELL_WIDTH].getIntValue();
			int ch = props.fields[SpriteStripDialog.CELL_HEIGHT].getIntValue();
			int x = props.fields[SpriteStripDialog.HOR_CELL_OFFSET].getIntValue() * cw;
			int y = props.fields[SpriteStripDialog.VERT_CELL_OFFSET].getIntValue() * ch;
			x += props.fields[SpriteStripDialog.HOR_PIXEL_OFFSET].getIntValue();
			y += props.fields[SpriteStripDialog.VERT_PIXEL_OFFSET].getIntValue();

			int xx = x, yy = y;
			for (int i = 0; i < props.fields[SpriteStripDialog.IMAGE_NUMBER].getIntValue(); i++)
				{
				if (i != 0 && i % props.fields[SpriteStripDialog.IMAGES_PER_ROW].getIntValue() == 0)
					{
					xx = x;
					yy += ch + props.fields[SpriteStripDialog.VERT_SEP].getIntValue();
					}
				g.drawRect(xx,yy,cw - 1,ch - 1);
				xx += cw + props.fields[SpriteStripDialog.HOR_SEP].getIntValue();
				}

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
		props.fields[SpriteStripDialog.HOR_CELL_OFFSET].setValue(0);
		props.fields[SpriteStripDialog.VERT_CELL_OFFSET].setValue(0);
		props.fields[SpriteStripDialog.HOR_PIXEL_OFFSET].setValue(x);
		props.fields[SpriteStripDialog.VERT_PIXEL_OFFSET].setValue(y);
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
		if (e.getID() == MouseEvent.MOUSE_DRAGGED && (e.getModifiers() | MouseEvent.BUTTON1_MASK) != 0)
			setBoundedOrigin(e.getX(),e.getY());
		super.processMouseMotionEvent(e);
		}

	protected BufferedImage getImage()
		{
		return props == null ? null : props.img;
		}

	public void propertyChange(PropertyChangeEvent evt)
		{
		updateUI();
		}
	}
