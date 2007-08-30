/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import static org.lateralgm.resources.Ref.deRef;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class GmObject extends Resource<GmObject>
	{
	public static final Ref<GmObject> OBJECT_SELF = new Ref<GmObject>(null);
	public static final Ref<GmObject> OBJECT_OTHER = new Ref<GmObject>(null);

	public static int refAsInt(Ref<GmObject> ref)
		{
		if (ref == OBJECT_SELF) return -1;
		if (ref == OBJECT_OTHER) return -2;
		if (deRef(ref) == null) return -100;
		return ref.getRes().getId();
		}

	public Ref<Sprite> sprite = null;
	public boolean solid = false;
	public boolean visible = true;
	public int depth = 0;
	public boolean persistent = false;
	public Ref<GmObject> parent = null;
	public Ref<Sprite> mask = null;
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
				Event ev2 = mev2.addEvent();
				ev2.mainId = ev.mainId;
				ev2.id = ev.id;
				ev2.other = ev.other;
				for (Action act : ev.actions)
					{
					Action act2 = ev2.addAction(act.libAction);
					act2.relative = act.relative;
					act2.not = act.not;
					act2.appliesTo = act.appliesTo;
					act2.arguments = new Argument[act.arguments.length];
					for (int l = 0; l < act.arguments.length; l++)
						act2.arguments[l] = new Argument(act.arguments[l].kind,act.arguments[l].val,
								act.arguments[l].res);
					}
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
