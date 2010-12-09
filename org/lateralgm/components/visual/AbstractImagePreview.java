/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public abstract class AbstractImagePreview extends JLabel
	{
	private static final long serialVersionUID = 1L;

	public AbstractImagePreview()
		{
		setOpaque(true);
		if (getImage() != null)
			{
			BufferedImage img = getImage();
			setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
			}
		else
			setPreferredSize(new Dimension(0,0));
		}

	protected abstract BufferedImage getImage();

	public Shape reclip(Graphics g)
		{
		Shape oldClip = g.getClip(); //backup the old clip
		Rectangle oldc = g.getClipBounds();
		//Set the clip properly
		BufferedImage img = getImage();
		g.setClip(new Rectangle(oldc.x,oldc.y,Math.min(oldc.x + oldc.width,img.getWidth()) - oldc.x,
				Math.min(oldc.y + oldc.height,img.getHeight()) - oldc.y));
		return oldClip;
		}

	public void setIcon(Icon ico)
		{
		super.setIcon(ico);
		if (ico != null)
			setPreferredSize(new Dimension(ico.getIconWidth(),ico.getIconHeight()));
		else
			setPreferredSize(new Dimension(0,0));
		}

	public void setImage(BufferedImage bi)
		{
		if (bi == null)
			{
			super.setIcon(null);
			setPreferredSize(new Dimension(0,0));
			return;
			}
		super.setIcon(new ImageIcon(bi));
		setPreferredSize(new Dimension(bi.getWidth(),bi.getHeight()));
		}
	}
