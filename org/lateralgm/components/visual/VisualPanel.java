/*
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
import java.util.TreeMap;

import javax.swing.JPanel;

import org.lateralgm.ui.swing.visuals.BoundedVisual;
import org.lateralgm.ui.swing.visuals.Visual;
import org.lateralgm.ui.swing.visuals.VisualContainer;

public class VisualPanel extends JPanel
	{
	private static final long serialVersionUID = 1L;
	private final TreeMap<Integer,Visual> visuals = new TreeMap<Integer,Visual>();
	private final Rectangle overallBounds = new Rectangle();
	public final VisualContainer container = new PanelVisualContainer();
	private boolean boundsLocked = false;
	private boolean boundsUpdated = false;

	public VisualPanel()
		{
		setBackground(Color.WHITE);
		}

	protected Rectangle getOverallBounds(Rectangle r)
		{
		if (r == null) return new Rectangle(overallBounds);
		r.setBounds(overallBounds);
		return r;
		}

	protected void calculateOverallBounds(Rectangle b)
		{
		b.grow(128,128);
		b.add(0,0);
		}

	@Override
	public Dimension getPreferredSize()
		{
		return overallBounds.getSize();
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
		public void repaint(Rectangle r)
			{
			if (r == null)
				VisualPanel.this.repaint();
			else
				VisualPanel.this.repaint(r.x - overallBounds.x,r.y - overallBounds.y,r.width,r.height);
			}

		public void updateBounds()
			{
			if (boundsLocked)
				{
				boundsUpdated = true;
				return;
				}
			Rectangle oob = overallBounds.getBounds();
			overallBounds.setSize(-1,-1);
			for (Visual v : visuals.values())
				if (v instanceof BoundedVisual) ((BoundedVisual) v).extendBounds(overallBounds);
			calculateOverallBounds(overallBounds);
			if (oob.equals(overallBounds)) return;
			Point p = getLocation();
			setBounds(p.x - oob.x + overallBounds.x,p.y - oob.y + overallBounds.y,Math.max(getWidth(),
					overallBounds.width),Math.max(getHeight(),overallBounds.height));
			revalidate();
			}
		}
	}
