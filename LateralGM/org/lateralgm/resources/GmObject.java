/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import static org.lateralgm.main.Util.deRef;

import java.lang.ref.WeakReference;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class GmObject extends Resource<GmObject>
	{
	public static final WeakReference<GmObject> OBJECT_SELF = new WeakReference<GmObject>(null);
	public static final WeakReference<GmObject> OBJECT_OTHER = new WeakReference<GmObject>(null);

	public static int refAsInt(WeakReference<GmObject> ref)
		{
		if (ref == OBJECT_SELF) return -1;
		if (ref == OBJECT_OTHER) return -2;
		if (deRef(ref) == null) return -100;
		return ref.get().getId();
		}

	public WeakReference<Sprite> sprite = null;
	public boolean solid = false;
	public boolean visible = true;
	public int depth = 0;
	public boolean persistent = false;
	public WeakReference<GmObject> parent = null;
	public WeakReference<Sprite> mask = null;
	public MainEvent[] mainEvents = new MainEvent[11];

	public GmObject()
		{
		setName(Prefs.prefixes[Resource.GMOBJECT]);
		for (int j = 0; j < 11; j++)
			{
			mainEvents[j] = new MainEvent();
			}
		}

	public GmObject copy()
		{
		return copy(false,null);
		}

	public GmObject copy(ResourceList<GmObject> src)
		{
		return copy(true,src);
		}

	private GmObject copy(boolean update, ResourceList<GmObject> src)
		{
		GmObject obj = new GmObject();
		obj.sprite = sprite;
		obj.solid = solid;
		obj.visible = visible;
		obj.depth = depth;
		obj.persistent = persistent;
		obj.parent = parent;
		obj.mask = mask;
		for (int i = 0; i < 11; i++)
			{
			MainEvent mev = mainEvents[i];
			MainEvent mev2 = obj.mainEvents[i];
			for (Event ev : mev.events)
				{
				mev2.events.add(ev.copy());
				}
			}
		if (update)
			{
			obj.setName(Prefs.prefixes[Resource.GMOBJECT] + (src.lastId + 1));
			src.add(obj);
			}
		else
			{
			obj.setId(getId());
			obj.setName(getName());
			}
		return obj;
		}

	public byte getKind()
		{
		return GMOBJECT;
		}
	}
