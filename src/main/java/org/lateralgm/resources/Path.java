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

import java.util.EnumMap;

import org.lateralgm.resources.sub.PathPoint;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.PropertyMap;

public class Path extends InstantiableResource<Path,Path.PPath>
	{
	public final ActiveArrayList<PathPoint> points = new ActiveArrayList<PathPoint>();

	public enum PPath
		{
		SMOOTH,CLOSED,PRECISION,BACKGROUND_ROOM,SNAP_X,SNAP_Y
		}

	private static final EnumMap<PPath,Object> DEFS = PropertyMap.makeDefaultMap(PPath.class,false,
			true,4,null,16,16);

	public Path()
		{
		this(null);
		}

	public Path(ResourceReference<Path> r)
		{
		super(r);
		}

	public Path makeInstance(ResourceReference<Path> r)
		{
		return new Path(r);
		}

	public PathPoint addPoint()
		{
		PathPoint point = new PathPoint();
		points.add(point);
		return point;
		}

	@Override
	protected void postCopy(Path dest)
		{
		super.postCopy(dest);
		for (PathPoint point : points)
			{
			PathPoint point2 = new PathPoint(point.getX(),point.getY(),point.getSpeed());
			dest.points.add(point2);
			}
		}

	@Override
	protected PropertyMap<PPath> makePropertyMap()
		{
		return new PropertyMap<PPath>(PPath.class,this,DEFS);
		}
	}
