/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Point;

public class Path extends Resource
	{
	public boolean smooth = false;
	public boolean closed = true;
	public int precision = 4;
	public ResId backgroundRoom = null;
	public int snapX = 16;
	public int snapY = 16;
	private ArrayList<Point> points = new ArrayList<Point>();

	public Path()
		{
		setName(Prefs.prefixes[Resource.PATH]);
		}

	public int NoPoints()
		{
		return points.size();
		}

	public Point addPoint()
		{
		Point point = new Point();
		points.add(point);
		return point;
		}

	public Point getPoint(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoPoints()) return points.get(ListIndex);
		return null;
		}

	public void removePoint(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoPoints()) points.remove(ListIndex);
		}

	public void clearPoints()
		{
		points.clear();
		}

	@SuppressWarnings("unchecked")
	public Path copy(boolean update, ResourceList src)
		{
		Path path = new Path();
		path.smooth = smooth;
		path.closed = closed;
		path.precision = precision;
		path.backgroundRoom = backgroundRoom;
		path.snapX = snapX;
		path.snapY = snapY;
		for (int i = 0; i < NoPoints(); i++)
			{
			Point point2 = path.addPoint();
			Point point = getPoint(i);
			point2.x = point.x;
			point2.y = point.y;
			point2.speed = point.speed;
			}
		if (update)
			{
			path.setId(new ResId(++src.lastId));
			path.setName(Prefs.prefixes[Resource.PATH] + src.lastId);
			src.add(path);
			}
		else
			{
			path.setId(getId());
			path.setName(getName());
			}
		return path;
		}
	}