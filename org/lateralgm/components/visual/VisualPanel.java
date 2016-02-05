/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * Contains all the methods for managing the zoom
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import static org.lateralgm.main.Util.negDiv;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.lateralgm.ui.swing.visuals.BoundedVisual;
import org.lateralgm.ui.swing.visuals.Visual;
import org.lateralgm.ui.swing.visuals.VisualContainer;

public class VisualPanel extends JPanel
	{
	private static final long serialVersionUID = 1L;

	public static final Point ORIGIN_MOUSE = new Point();

	private final TreeMap<Integer,Visual> visuals = new TreeMap<Integer,Visual>();
	private final Rectangle overallBounds = new Rectangle();
	public final VisualContainer container = new PanelVisualContainer();
	private int zoom = 1;
	protected Point zoomOrigin;
	private boolean boundsLocked = false;
	private boolean boundsUpdated = false;

	public VisualPanel()
		{
		setBackground(Color.GRAY);
		setOpaque(true);
		}

	protected void componentToVisual(Point p)
		{
		componentToVisual(p,zoom);
		}

	protected void componentToVisual(Point p, int z)
		{
		p.x = zoom(p.x - visualOffsetX(z),2 - z) + overallBounds.x;
		p.y = zoom(p.y - visualOffsetY(z),2 - z) + overallBounds.y;
		}

	protected void componentToVisual(Rectangle r, int z)
		{
		Point p = r.getLocation();
		componentToVisual(p,z);
		r.setLocation(p);
		r.width = zoom(r.width,2 - z);
		r.height = zoom(r.height,2 - z);
		}

	public void visualToComponent(Point p)
		{
		visualToComponent(p,zoom);
		}

	public void visualToComponent(Point p, int z)
		{
		p.x = zoom(p.x - overallBounds.x,z) + visualOffsetX(z);
		p.y = zoom(p.y - overallBounds.y,z) + visualOffsetY(z);
		}

	protected void visualToComponent(Rectangle r, int z)
		{
		Point p = r.getLocation();
		visualToComponent(p,z);
		r.setLocation(p);
		r.width = zoom(r.width,z);
		r.height = zoom(r.height,z);
		}

	public Rectangle getOverallBounds()
		{
		return overallBounds;
		}

	protected static double zoom(double d, int z)
		{
		return z > 0 ? z * d : d / (2 - z);
		}

	protected static int zoom(int i, int z)
		{
		return z > 0 ? z * i : negDiv(i,2 - z);
		}

	protected static int zoomAlign(int i, int z, boolean ceil)
		{
		return z <= 0 ? (2 - z) * negDiv(i + (ceil ? 1 - z : 0),2 - z) : i;
		}

	protected static void zoomAlign(Rectangle r, int z, boolean out)
		{
		int x0 = zoomAlign(r.x,z,!out);
		int y0 = zoomAlign(r.y,z,!out);
		r.setBounds(x0,y0,zoomAlign(r.width + r.x - x0,z,out),zoomAlign(r.height + r.y - y0,z,out));
		}

	protected static void zoom(Rectangle r, int z)
		{
		r.x = zoom(r.x,z);
		r.y = zoom(r.y,z);
		r.width = zoom(r.width,z);
		r.height = zoom(r.height,z);
		}

	protected int visualOffsetX(int z)
		{
		return (getWidth() - zoom(overallBounds.width,z)) / 2;
		}

	protected int visualOffsetY(int z)
		{
		return (getHeight() - zoom(overallBounds.height,z)) / 2;
		}

	protected Rectangle getOverallBounds(Rectangle r)
		{
		if (r == null) return new Rectangle(overallBounds);
		r.setBounds(overallBounds);
		return r;
		}

	protected static void calculateOverallBounds(Rectangle b)
		{
		b.grow(128,128);
		b.add(0,0);
		}

	@Override
	public Dimension getPreferredSize()
		{
		Dimension s = overallBounds.getSize();
		s.width = zoom(s.width,zoom);
		s.height = zoom(s.height,zoom);
		return s;
		}

	@Override
	public Dimension getMinimumSize()
		{
		return getPreferredSize();
		}

	@Override
	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);

		Graphics g2 = g.create();
		if (g2.getClip() == null) g2.setClip(0,0,getWidth(),getHeight());
		g2.translate(visualOffsetX(zoom),visualOffsetY(zoom));
		if (zoom != 1)
			{
			double s = zoom(1.0,zoom);
			Graphics2D g3 = ((Graphics2D) g2);
			g3.scale(s,s);
			g3.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				zoom < 1 ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
				: RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			}
		g2.translate(-overallBounds.x,-overallBounds.y);
		paintVisuals(g2);
		g2.dispose();
		}

	protected void paintVisuals(Graphics g)
		{
		for (Visual v : visuals.values())
			v.paint(g);
		}

	public void put(int layer, Visual v)
		{
		Visual pv = v == null ? visuals.remove(layer) : visuals.put(layer,v);
		if (pv == v) return;
		if (v instanceof BoundedVisual || pv instanceof BoundedVisual) container.updateBounds();
		repaint();
		}

	protected void lockBounds()
		{
		boundsLocked = true;
		}

	protected void unlockBounds()
		{
		boundsLocked = false;
		if (boundsUpdated)
			{
			container.updateBounds();
			boundsUpdated = false;
			}
		}

	private class PanelVisualContainer implements VisualContainer
		{
		private int oldZoom = zoom;

		public void repaint(Rectangle r)
			{
			if (r == null)
				VisualPanel.this.repaint();
			else
				{
				Rectangle cr = r.getBounds();
				zoomAlign(cr,zoom,true);
				visualToComponent(cr,zoom);
				VisualPanel.this.repaint(cr);
				}
			}

		public void updateBounds()
			{
			boolean uob = (!boundsLocked);
			if (boundsLocked) boundsUpdated = true;
			boolean uz = zoom != oldZoom;
			if (!uob && !uz) return;
			Point o = zoomOrigin;
			Point co;
			if (o == null || o == ORIGIN_MOUSE)
				{
				co = o == null ? null : getMousePosition();
				if (co == null)
					{
					Rectangle vr = getVisibleRect();
					co = new Point(vr.x + vr.width / 2,vr.y + vr.height / 2);
					}
				o = co.getLocation();
				componentToVisual(o,oldZoom);
				}
			else
				{
				co = o.getLocation();
				visualToComponent(co,oldZoom);
				}
			Rectangle oob = overallBounds.getBounds();
			if (uob)
				{
				overallBounds.setSize(-1,-1);
				for (Visual v : visuals.values())
					if (v instanceof BoundedVisual) ((BoundedVisual) v).extendBounds(overallBounds);
				calculateOverallBounds(overallBounds);
				}
			zoomAlign(overallBounds,zoom,true);
			if (oob.equals(overallBounds) && !uz) return;
			Point p = getLocation();
			setBounds(p.x + co.x - zoom(o.x - overallBounds.x,zoom),
					p.y + co.y - zoom(o.y - overallBounds.y,zoom),
					Math.max(getWidth(),zoom(overallBounds.width,zoom)),
					Math.max(getHeight(),zoom(overallBounds.height,zoom)));
			oldZoom = zoom;
			revalidate();
			}
		}

	public void setZoom(int z)
		{
		if (zoom == z) return;
		zoom = z;
		container.updateBounds();
		}
	}
