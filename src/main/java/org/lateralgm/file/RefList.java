/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.util.Hashtable;

import org.lateralgm.resources.InstantiableResource;
import org.lateralgm.resources.ResourceReference;

public class RefList<R extends InstantiableResource<R,?>>
	{
	private Hashtable<Integer,ResRef<R>> rrt = new Hashtable<Integer,ResRef<R>>();
	private Class<R> clazz;

	public RefList(Class<R> clazz)
		{
		this.clazz = clazz;
		}

	public ResourceReference<R> get(int id)
		{
		if (id < 0) return null;
		ResRef<R> rr = rrt.get(id);
		if (rr != null) return rr.reference;
		R r = null;
		try
			{
			r = clazz.newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		if (r != null)
			{
			rr = new ResRef<R>(r);
			rrt.put(id,rr);
			r.setId(id);
			return rr.reference;
			}
		return null;
		}

	private static class ResRef<R extends InstantiableResource<R,?>>
		{
		ResourceReference<R> reference;
		/**
		 * Keep a hard reference so it doesn't get lost/destroyed.<br />
		 * At this time, the hard reference has no other purpose, so remains unused.
		 * @since r228
		 */
		@SuppressWarnings("unused")
		R resource;

		public ResRef(R res)
			{
			resource = res;
			reference = res.reference;
			}
		}
	}
