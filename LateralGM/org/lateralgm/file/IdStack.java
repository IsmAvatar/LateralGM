/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.util.ArrayList;

import org.lateralgm.resources.ResId;

public class IdStack
	{
	private ArrayList<ResId> ids = new ArrayList<ResId>();

	public ResId get(int id)
		{
		if (id < 0) return null;
		for (ResId i : ids)
			if (i.getValue() == id) return i;
		ResId newid = new ResId(id);
		ids.add(newid);
		return newid;
		}
	}
