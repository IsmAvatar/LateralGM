/*
 * Copyright (C) 2012 Medo <smaxein@googlemail.com> 
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.visuals;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BinVisual extends AbstractVisual implements VisualContainer,BoundedVisual
	{
	private final Map<Visual,VisualItem> itemMap = new HashMap<Visual,VisualItem>();
	private final SpatialHashMap spatialHashMap = new SpatialHashMap();
	private Rectangle overallBounds = new Rectangle();

	public BinVisual(VisualContainer c)
		{
		super(c);
		}

	public void add(Visual v, Rectangle b, int d)
		{
		VisualItem item = new VisualItem(v,b,d);
		VisualItem previous = itemMap.put(v,item);
		if (previous != null)
			spatialHashMap.remove(previous);
		spatialHashMap.put(item);

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
		spatialHashMap.remove(item);
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
		for (VisualItem item : spatialHashMap.intersect(r))
			result.add(item.visual);
		return result.iterator();
		}

	@SuppressWarnings("unchecked")
	public <V extends Visual>Iterator<V> intersect(Rectangle r, final Class<V> v)
		{
		List<V> result = new ArrayList<V>();
		for (VisualItem item : spatialHashMap.intersect(r))
			if (v.isInstance(item.visual))
				result.add((V) item.visual);
		return result.iterator();
		}

	public void paint(Graphics g)
		{
		Rectangle clip = g.getClipBounds();
		List<VisualItem> items = spatialHashMap.intersect(clip);
		Collections.sort(items,new InverseDepthComparator());
		for (VisualItem item : items)
			{
			Rectangle b = item.bounds;
			Graphics g2 = g.create(b.x,b.y,b.width,b.height);
			item.visual.paint(g2);
			g2.dispose();
			}
		}

	public void invalidateBounds()
		{
		overallBounds = null;
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
			for (VisualItem item : itemMap.values())
				if (item.bounds != null)
					overallBounds.add(item.bounds);
			}
		b.add(overallBounds);
		}

	@Override
	public void repaint(Rectangle r)
		{
			super.repaint(r);
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

	private static class VisualItem
		{
		final Visual visual;
		// bounds is intended immutable, and SpatialHashMap relies on that - don't change it!
		final Rectangle bounds;
		final int depth;

		public VisualItem(Visual visual, Rectangle bounds, int depth)
			{
			this.visual = visual;
			this.bounds = bounds == null ? null : new Rectangle(bounds);
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

	/**
	 * Threadsafe
	 */
	private static class SpatialHashMap
		{
		private static final int BIN_SIZE = 128;
		private HashMap<Point,Set<VisualItem>> binMap = new HashMap<Point,Set<VisualItem>>();

		/**
		 * @return true if the item was added to the map,
		 *         false if it has no bounds or empty bounds, or if the item was already mapped
		 */
		synchronized boolean put(VisualItem item)
			{
			if (item.bounds == null || item.bounds.isEmpty()) return false;
			for (Point key : overlappingBins(item.bounds))
				{
				Set<VisualItem> bin = binMap.get(key);
				if (bin == null)
					{
					bin = new HashSet<VisualItem>();
					binMap.put(key,bin);
					}
				if (!bin.add(item))
					return false;
				}
			return true;
			}

		/**
		 * @return true if item was removed, false if it was not mapped.
		 */
		synchronized boolean remove(VisualItem item)
			{
			if (item.bounds == null || item.bounds.isEmpty()) return false;
			for (Point key : overlappingBins(item.bounds))
				{
				Set<VisualItem> bin = binMap.get(key);
				if (bin == null || !bin.remove(item))
					return false;
				if (bin.isEmpty())
					binMap.remove(key);
				}
			return true;
			}

		/**
		 * @return All mapped VisualItems whose bounds intersect r.
		 */
		public List<VisualItem> intersect(Rectangle r)
			{
			Set<VisualItem> result1 = new HashSet<VisualItem>();
			synchronized (this)
				{
				for (Point key : overlappingBins(r))
					{
					Set<VisualItem> bin = binMap.get(key);
					if (bin != null)
						result1.addAll(bin);
					}
				}

			List<VisualItem> result2 = new ArrayList<VisualItem>();
			for (Iterator<VisualItem> iter = result1.iterator(); iter.hasNext();)
				{
				VisualItem item = iter.next();
				if (r.intersects(item.bounds))
					result2.add(item);
				}
			return result2;
			}

		/**
		 * @return A collection of Points (keys to binMap), corresponding to the bins overlapped by rect.
		 */
		private Collection<Point> overlappingBins(Rectangle rect)
			{
			if (rect.isEmpty()) return Collections.emptyList();
			final int binx = calculateBinCoord(rect.x);
			final int biny = calculateBinCoord(rect.y);
			final int binwidth = calculateBinCoord(rect.x + rect.width - 1) - binx + 1;
			final int binheight = calculateBinCoord(rect.y + rect.height - 1) - biny + 1;
			return new AbstractList<Point>()
				{
					@Override
					public Point get(int index)
						{
						if (index < 0 || index >= size())
							throw new IndexOutOfBoundsException();
						return new Point(binx + (index % binwidth),biny + (index / binwidth));
						}

					@Override
					public int size()
						{
						return binwidth * binheight;
						}
				};
			}

		/**
		 * This function calculates the bin coordinate for a pixel coordinate.
		 * With a bin size of 100, this maps coordinates the following way:
		 * [-200, -101] -> -2
		 * [-100, -1] -> -1
		 * [0, 99] -> 0
		 * [100, 199] -> 1
		 * and so on.
		 */
		private int calculateBinCoord(int x)
			{
			return x >= 0 ? x / BIN_SIZE : ~(~x / BIN_SIZE);
			}
		}
	}