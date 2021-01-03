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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.lateralgm.components.NumberField.ValueChangeEvent;
import org.lateralgm.components.NumberField.ValueChangeListener;
import org.lateralgm.components.impl.SpriteStripDialog;

public class SpriteStripPreview extends AbstractImagePreview implements ValueChangeListener
	{
	private static final long serialVersionUID = 1L;

	private final SpriteStripDialog props;

	public SpriteStripPreview(SpriteStripDialog sd)
		{
		super();
		props = sd;
		enableEvents(MouseEvent.MOUSE_PRESSED);
		enableEvents(MouseEvent.MOUSE_DRAGGED);
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);

		if (getImage() == null) return; // << super paint already sized to 0x0
		g = g.create(); // << clone for safety
		reclipAndTranslate(g); // << intersection clip & center

		g.setXORMode(Color.BLACK); //XOR mode so that bbox and origin can counter
		g.setColor(Color.WHITE);

		for (Rectangle r : props)
			g.drawRect(r.x,r.y,r.width - 1,r.height - 1);

		g.dispose(); // cleanup
		}

	private void setBoundedOrigin(int x, int y)
		{
		Dimension d = getPreferredSize();
		x = Math.max(0,Math.min(d.width - 1,x));
		y = Math.max(0,Math.min(d.height - 1,y));
		props.setOrigin(x,y);
		}

	protected void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1
				&& e.getX() < getPreferredSize().width && e.getY() < getPreferredSize().height)
			{
			Point pnt = this.translatePoint(e.getPoint());
			setBoundedOrigin(pnt.x,pnt.y);
			}
		super.processMouseEvent(e);
		}

	protected void processMouseMotionEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_DRAGGED && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
			{
			Point pnt = this.translatePoint(e.getPoint());
			setBoundedOrigin(pnt.x,pnt.y);
			}
		super.processMouseMotionEvent(e);
		}

	protected BufferedImage getImage()
		{
		return props == null ? null : props.img;
		}

	public void valueChange(ValueChangeEvent evt)
		{
		repaint();
		}
	}
