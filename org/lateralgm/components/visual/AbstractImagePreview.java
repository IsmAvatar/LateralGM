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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.lateralgm.main.Util;

public abstract class AbstractImagePreview extends JLabel
	{
	private static final long serialVersionUID = 1L;

	private BufferedImage transparentBackground = null;
	protected double zoom = 1.0;

	public AbstractImagePreview()
		{
		setOpaque(true);
		}

	protected abstract BufferedImage getImage();

	public Point getTopLeftCentered()
		{
		Dimension d = getPreferredSize();
		return new Point(this.getWidth() / 2 - d.width / 2,this.getHeight() / 2 - d.height / 2);
		}

	public Point translatePoint(Point p) 
		{
		Point origin = getTopLeftCentered();
		Point t = new Point
			(
			(int)Math.floor((p.x / zoom) - (origin.x / zoom)),
			(int)Math.floor((p.y / zoom) - (origin.y / zoom))
			);
		return t;
		}

	public Shape reclipAndTranslate(Graphics g)
		{
		Shape oldClip = g.getClip(); //backup the old clip
		Dimension prefSize = getPreferredSize();
		Graphics2D g2d = (Graphics2D) g;
		Point p = getTopLeftCentered();
		g2d.translate(p.x,p.y);
		g.clipRect(0,0,prefSize.width,prefSize.height);
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
		scroll.getViewport().setViewPosition(new Point(newX,newY));
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
		scroll.getViewport().setViewPosition(new Point(newX,newY));
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

		BufferedImage image = this.getImage();
		if (image == null)
			{
			setPreferredSize(new Dimension(0,0));
			return;
			}
		g = g.create(); // clone for safety
		reclipAndTranslate(g);

		Dimension prefSize = getPreferredSize();
		int width = (int)Math.ceil(prefSize.getWidth() / 10f);
		int height = (int)Math.ceil(prefSize.getHeight() / 10f);
		width = width < 1 ? 1 : width;
		height = height < 1 ? 1 : height;
		if (transparentBackground == null || width != transparentBackground.getWidth() ||
			height != transparentBackground.getHeight())
			transparentBackground = Util.paintBackground(width, height);

		g.drawImage(transparentBackground, 0, 0, transparentBackground.getWidth() * 10,
			transparentBackground.getHeight() * 10, null);

		g.drawImage(image,0,0,prefSize.width,prefSize.height,null);

		g.dispose(); // cleanup
		}
	}
