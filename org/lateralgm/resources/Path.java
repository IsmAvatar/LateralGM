/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;
import java.util.EnumMap;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.util.PropertyMap;

public class Path extends Resource<Path,Path.PPath>
	{
	public ArrayList<PathPoint> points = new ArrayList<PathPoint>();

	public enum PPath
		{
		SMOOTH,CLOSED,PRECISION,BACKGROUND_ROOM,SNAP_X,SNAP_Y
		}

	private static final EnumMap<PPath,Object> DEFS = PropertyMap.makeDefaultMap(PPath.class,false,
			true,4,null,16,16);

	public Path()
		{
		this(null,true);
		}

	public Path(ResourceReference<Path> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes.get(Kind.PATH));
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
		copy(src,p);
		for (PathPoint point : points)
			{
			PathPoint point2 = p.addPoint();
			point2.x = point.x;
			point2.y = point.y;
			point2.speed = point.speed;
			}
		return p;
		}

	public Kind getKind()
		{
		return Kind.PATH;
		}

	@Override
	protected PropertyMap<PPath> makePropertyMap()
		{
		return new PropertyMap<PPath>(PPath.class,this,DEFS);
		}
	}
