/*
 * Copyright (C) 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public abstract class AbstractImagePreview extends JLabel
	{
	private static final long serialVersionUID = 1L;

	protected double zoom = 1.0;

	public AbstractImagePreview()
		{
		setOpaque(true);
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

	public Dimension getPreferredSize()
		{
		BufferedImage bi = getImage();
		if (bi == null) return super.getPreferredSize();
		return new Dimension((int)(bi.getWidth() * zoom),(int)(bi.getHeight() * zoom));
		}

	public double getZoom()
		{
		return zoom;
		}

	public void setZoom(double nzoom)
		{
		zoom = nzoom;
		this.resizeAndRepaint();
		}

	/** Zoom in, centering around a specific point, usually the mouse. */
	public void zoomIn(Point point, JScrollPane scroll)
		{
		if (this.getZoom() >= 32) return;
		this.setZoom(this.getZoom() * 2);
		scroll.validate();
		Dimension size = scroll.getViewport().getSize();

		int newX = (int) (point.x * 2) - size.width / 2;
		int newY = (int) (point.y * 2) - size.height / 2;
		setLocation(new Point(-newX,-newY));
		}

	/** Zoom out, centering around a specific point, usually the mouse. */
	public void zoomOut(Point point, JScrollPane scroll)
		{
		if (this.getZoom() <= 0.04) return;
		this.setZoom(this.getZoom() / 2);
		scroll.validate();
		Dimension size = scroll.getViewport().getSize();

		int newX = (int) (point.x / 2) - size.width / 2;
		int newY = (int) (point.y / 2) - size.height / 2;
		setLocation(new Point(-newX,-newY));
		}

	public void zoomIn(JScrollPane scroll)
		{
		Dimension size = this.getPreferredSize();
		zoomIn(new Point(size.width/2,size.height/2),scroll);
		}

	public void zoomOut(JScrollPane scroll)
		{
		Dimension size = this.getPreferredSize();
		zoomOut(new Point(size.width/2,size.height/2),scroll);
		}

	/**
	 * Refreshes the image preview by laying it out to its
	 * preferred dimensions and then repainting it. Follows the
	 * same conventions that {@code JTable.resizeAndRepaint} and
	 * other Swing components do internally. It is not to be
	 * used when the size hasn't actually changed, e.g
	 * transparency pixel removed. In that case, only a simple
	 * repaint is all that is needed.
	 */
	protected void resizeAndRepaint()
		{
		revalidate(); // size change, do layout
		repaint(); // repaint it with new size
		}

	@Deprecated
	public void setIcon(Icon ico)
		{
		super.setIcon(ico);
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		g.drawImage(getImage(),0,0,null);
		}
	}
