/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

public class PathPoint
	{
	public int x = 0;
	public int y = 0;
	public int speed = 100;

	public PathPoint()
		{
		}

	public PathPoint(int x, int y, int speed)
		{
		this.x = x;
		this.y = y;
		this.speed = speed;
		}

	public String toString()
		{
		String r = "(" + x + "," + y + ")";
		while (r.length() < 11)
			r += " ";
		return r + " sp: " + speed;
		}
	}
