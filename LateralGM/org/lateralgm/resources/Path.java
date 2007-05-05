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
	public boolean Smooth = false;
	public boolean Closed = true;
	public int Precision = 4;
	public ResId BackgroundRoom = null;
	public int SnapX = 16;
	public int SnapY = 16;
	private ArrayList<Point> Points = new ArrayList<Point>();

	public Path()
		{
		name = Prefs.prefixes[Resource.PATH];
		}

	public int NoPoints()
		{
		return Points.size();
		}

	public Point addPoint()
		{
		Point point = new Point();
		Points.add(point);
		return point;
		}

	public Point getPoint(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoPoints()) return Points.get(ListIndex);
		return null;
		}

	public void removePoint(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoPoints()) Points.remove(ListIndex);
		}

	public void clearPoints()
		{
		Points.clear();
		}

	public Path copy(boolean update, ResourceList src)
		{
		Path path = new Path();
		path.Smooth = Smooth;
		path.Closed = Closed;
		path.Precision = Precision;
		path.BackgroundRoom = BackgroundRoom;
		path.SnapX = SnapX;
		path.SnapY = SnapY;
		for (int i = 0; i < NoPoints(); i++)
			{
			Point point2 = path.addPoint();
			Point point = getPoint(i);
			point2.X = point.X;
			point2.Y = point.Y;
			point2.Speed = point.Speed;
			}
		if (update)
			{
			path.Id.value = ++src.LastId;
			path.name = Prefs.prefixes[Resource.PATH] + src.LastId;
			src.add(path);
			}
		else
			{
			path.Id = Id;
			path.name = name;
			}
		return path;
		}
	}