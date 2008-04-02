/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Point;

public class Path extends Resource<Path>
	{
	public boolean smooth = false;
	public boolean closed = true;
	public int precision = 4;
	public WeakReference<Room> backgroundRoom = null;
	public int snapX = 16;
	public int snapY = 16;
	public ArrayList<Point> points = new ArrayList<Point>();

	public Path()
		{
		setName(Prefs.prefixes[Resource.PATH]);
		}

	public Point addPoint()
		{
		Point point = new Point();
		points.add(point);
		return point;
		}

	public Path copy()
		{
		return copy(false,null);
		}

	public Path copy(ResourceList<Path> src)
		{
		return copy(true,src);
		}

	private Path copy(boolean update, ResourceList<Path> src)
		{
		Path path = new Path();
		path.smooth = smooth;
		path.closed = closed;
		path.precision = precision;
		path.backgroundRoom = backgroundRoom;
		path.snapX = snapX;
		path.snapY = snapY;
		for (Point point : points)
			{
			Point point2 = path.addPoint();
			point2.x = point.x;
			point2.y = point.y;
			point2.speed = point.speed;
			}
		if (update)
			{
			path.setName(Prefs.prefixes[Resource.PATH] + (src.lastId + 1));
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
