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

	public int noPoints()
		{
		return points.size();
		}

	public Point addPoint()
		{
		Point point = new Point();
		points.add(point);
		return point;
		}

	public Point getPoint(int listIndex)
		{
		if (listIndex >= 0 && listIndex < noPoints()) return points.get(listIndex);
		return null;
		}

	public void removePoint(int listIndex)
		{
		if (listIndex >= 0 && listIndex < noPoints()) points.remove(listIndex);
		}

	public void clearPoints()
		{
		points.clear();
		}

	public Path copy()
		{
		return copy(false,null);
		}

	@SuppressWarnings("unchecked")
	public Path copy(ResourceList src)
		{
		return copy(true,src);
		}

	@SuppressWarnings("unchecked")
	private Path copy(boolean update, ResourceList src)
		{
		Path path = new Path();
		path.smooth = smooth;
		path.closed = closed;
		path.precision = precision;
		path.backgroundRoom = backgroundRoom;
		path.snapX = snapX;
		path.snapY = snapY;
		for (int i = 0; i < noPoints(); i++)
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

	public byte getKind()
		{
		return PATH;
		}
	}
