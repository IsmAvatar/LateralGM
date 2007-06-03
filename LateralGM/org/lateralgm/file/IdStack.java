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
