/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.PathPoint;

public class Path extends Resource<Path>
	{
	public boolean smooth = false;
	public boolean closed = true;
	public int precision = 4;
	public ResourceReference<Room> backgroundRoom = null;
	public int snapX = 16;
	public int snapY = 16;
	public ArrayList<PathPoint> points = new ArrayList<PathPoint>();

	public Path()
		{
		this(null,true);
		}

	public Path(ResourceReference<Path> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes[Resource.PATH]);
		}

	public PathPoint addPoint()
		{
		PathPoint point = new PathPoint();
		points.add(point);
		return point;
		}

	@Override
	protected Path copy(ResourceList<Path> src, ResourceReference<Path> ref, boolean update)
		{
		Path p = new Path(ref,update);
		p.smooth = smooth;
		p.closed = closed;
		p.precision = precision;
		p.backgroundRoom = backgroundRoom;
		p.snapX = snapX;
		p.snapY = snapY;
		for (PathPoint point : points)
			{
			PathPoint point2 = p.addPoint();
			point2.x = point.x;
			point2.y = point.y;
			point2.speed = point.speed;
			}
		if (src != null)
			{
			p.setName(Prefs.prefixes[Resource.PATH] + (src.lastId + 1));
			src.add(p);
			}
		else
			{
			p.setId(getId());
			p.setName(getName());
			}
		return p;
		}

	public byte getKind()
		{
		return PATH;
		}
	}
