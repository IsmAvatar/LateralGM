/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

public class Point
	{
	public int x = 0;
	public int y = 0;
	public int speed = 100;

	public Point()
		{
		}

	public Point(int x, int y, int speed)
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
