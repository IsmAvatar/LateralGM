/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.util.ArrayList;

import org.lateralgm.resources.Ref;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;

public class RefList<R extends Resource<R>>
	{
	private ArrayList<Ref<R>> ids = new ArrayList<Ref<R>>();
	private Class<R> clazz;
	private GmFile parent;

	public RefList(Class<R> clazz, GmFile parent)
		{
		this.clazz = clazz;
		this.parent = parent;
		}

	public Ref<R> get(int id)
		{
		if (id < 0) return null;
		for (Ref<R> r : ids)
			if (r.getRes().getId() == id) return r;
		Ref<R> newid = null;
		try
			{
			if (clazz == Room.class)
				newid = new Ref<R>(clazz.getConstructor(GmFile.class).newInstance(parent));
			else
				newid = new Ref<R>(clazz.newInstance());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		if (newid != null)
			{
			ids.add(newid);
			newid.getRes().setId(id);
			}
		return newid;
		}
	}
