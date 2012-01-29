/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class BinVisual extends AbstractVisual implements VisualContainer,BoundedVisual
	{
	public static class MockVisual implements Visual
		{
		public void paint(Graphics g)
			{
			}
		}

	private static class VisualItem
		{
		final Visual visual;
		final Rectangle bounds;
		final int depth;
	
		public VisualItem(Visual visual, Rectangle bounds, int depth)
			{
			this.visual = visual;
			if (bounds == null)
				this.bounds = null;
			else
				{
				this.bounds = new Rectangle(bounds);
				}
			this.depth = depth;
			}
		}

	private static class InverseDepthComparator implements Comparator<VisualItem>
		{
		public int compare(VisualItem o1, VisualItem o2)
			{
			if (o1.depth > o2.depth)
				return -1;
			else if (o1.depth < o2.depth)
				return 1;
			else
				return System.identityHashCode(o1) - System.identityHashCode(o2);
			}
		}

	private final Map<Visual,VisualItem> itemMap = new HashMap<Visual,BinVisual.VisualItem>();
	private final SortedSet<VisualItem> depthSortedItems = new TreeSet<VisualItem>(
			new InverseDepthComparator());
	private Rectangle overallBounds = new Rectangle();

	public BinVisual(VisualContainer c)
		{
		super(c);
		}

	@Override
	public void repaint(Rectangle r)
		{
		if (parent != null)
			super.repaint(r);
		}

	public void add(Visual v, Rectangle b, int d)
		{
		VisualItem item = new VisualItem(v,b,d);
		VisualItem previous = itemMap.put(v,item);
		if (previous != null)
			depthSortedItems.remove(previous);
		if (b != null)
			depthSortedItems.add(item);

		if (item.bounds != null)
			repaint(item.bounds);
		if (!equalBounds(item,previous))
			{
			if (previous != null && previous.bounds != null)
				repaint(previous.bounds);
			invalidateBounds();
			}
		}

	public boolean remove(Visual v)
		{
		VisualItem item = itemMap.remove(v);
		if (item == null) return false;
		depthSortedItems.remove(item);
		if (item.bounds != null)
			{
			repaint(item.bounds);
			invalidateBounds();
			}
		return true;
		}

	public void setDepth(Visual v, int d)
		{
		VisualItem item = itemMap.get(v);
		if (item == null) return;
		add(v,item.bounds,d);
		}

	public void setBounds(Visual v, Rectangle b)
		{
		VisualItem item = itemMap.get(v);
		if (item == null) return;
		add(v,b,item.depth);
		}

	public Iterator<Visual> intersect(Rectangle r)
		{
		List<Visual> result = new ArrayList<Visual>();
		for (VisualItem item : depthSortedItems)
			if (r.intersects(item.bounds))
				result.add(item.visual);
		return result.iterator();
		}

	@SuppressWarnings("unchecked")
	public <V extends Visual>Iterator<V> intersect(Rectangle r, final Class<V> v)
		{
		List<V> result = new ArrayList<V>();
		for (VisualItem item : depthSortedItems)
			if (v.isInstance(item.visual) && r.intersects(item.bounds))
				result.add((V) item.visual);
		return result.iterator();
		}

	public void paint(Graphics g)
		{
		Rectangle clip = g.getClipBounds();
		for (VisualItem item : depthSortedItems)
			{
			Rectangle b = item.bounds;
			if (b.intersects(clip))
				{
				Graphics g2 = g.create(b.x,b.y,b.width,b.height);
				item.visual.paint(g2);
				g2.dispose();
				}
			}
		}

	public void invalidateBounds()
		{
		overallBounds = null;
		if (parent != null)
			parent.updateBounds();
		}

	public void updateBounds()
		{
		// Unused
		}

	public void extendBounds(Rectangle b)
		{
		if (overallBounds == null)
			{
			overallBounds = new Rectangle();
			for (VisualItem item : depthSortedItems)
				overallBounds.add(item.bounds);
			}
		b.add(overallBounds);
		}

	private boolean equalRect(Rectangle r1, Rectangle r2)
		{
		if (r1 == r2)
			return true;
		if (r1 == null || r2 == null)
			return false;
		return r1.equals(r2);
		}

	private boolean equalBounds(VisualItem i1, VisualItem i2)
		{
		Rectangle b1 = i1 == null ? null : i1.bounds;
		Rectangle b2 = i2 == null ? null : i2.bounds;
		return equalRect(b1,b2);
		}
	}
