/*
 * Copyright (C) 2007 Clam
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * 
 * Lateral GM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Lateral GM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lateral GM; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.lateralgm.resources;

import org.lateralgm.file.ResourceList;

//TODO Implement Resource.equals method
public abstract class Resource implements Comparable<Resource>
	{
	public static final byte SPRITE = 2;
	public static final byte SOUND = 3;
	public static final byte BACKGROUND = 6;
	public static final byte PATH = 8;
	public static final byte SCRIPT = 7;
	public static final byte FONT = 9;
	public static final byte TIMELINE = 12;
	public static final byte GMOBJECT = 1;
	public static final byte ROOM = 4;

	public static final byte GAMEINFO = 10;
	public static final byte GAMESETTINGS = 11;

	public ResId Id = new ResId(0);
	public String name = "";

	public int compareTo(Resource res)
		{
		return res.Id.value == Id.value ? 0 : (res.Id.value < Id.value ? -1 : 1);
		}

	public abstract Resource copy(boolean update, ResourceList src);
	}