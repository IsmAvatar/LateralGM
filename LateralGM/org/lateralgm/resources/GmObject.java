/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import static org.lateralgm.main.Util.deRef;

import java.awt.image.BufferedImage;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.Util;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;

public class GmObject extends Resource<GmObject>
	{
	public static final ResourceReference<GmObject> OBJECT_SELF = new ResourceReference<GmObject>(
			null);
	public static final ResourceReference<GmObject> OBJECT_OTHER = new ResourceReference<GmObject>(
			null);

	public static int refAsInt(ResourceReference<GmObject> ref)
		{
		if (ref == OBJECT_SELF) return -1;
		if (ref == OBJECT_OTHER) return -2;
		if (deRef(ref) == null) return -100;
		return ref.get().getId();
		}

	public ResourceReference<Sprite> sprite = null;
	public boolean solid = false;
	public boolean visible = true;
	public int depth = 0;
	public boolean persistent = false;
	public ResourceReference<GmObject> parent = null;
	public ResourceReference<Sprite> mask = null;
	public MainEvent[] mainEvents = new MainEvent[11];

	public GmObject()
		{
		this(null,true);
		}

	public GmObject(ResourceReference<GmObject> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes[Resource.GMOBJECT]);
		for (int j = 0; j < 11; j++)
			{
			mainEvents[j] = new MainEvent();
			}
		}

	@Override
	protected GmObject copy(ResourceList<GmObject> src, ResourceReference<GmObject> ref,
			boolean update)
		{
		GmObject o = new GmObject(ref,update);
		o.sprite = sprite;
		o.solid = solid;
		o.visible = visible;
		o.depth = depth;
		o.persistent = persistent;
		o.parent = parent;
		o.mask = mask;
		for (int i = 0; i < 11; i++)
			{
			MainEvent mev = mainEvents[i];
			MainEvent mev2 = o.mainEvents[i];
			for (Event ev : mev.events)
				{
				mev2.events.add(ev.copy());
				}
			}
		if (src != null)
			{
			o.setName(Prefs.prefixes[Resource.GMOBJECT] + (src.lastId + 1));
			src.add(o);
			}
		else
			{
			o.setId(getId());
			o.setName(getName());
			}
		return o;
		}

	@Override
	public BufferedImage getDisplayImage()
		{
		Sprite s = Util.deRef(sprite);
		return s == null ? null : s.getDisplayImage();
		}

	public byte getKind()
		{
		return GMOBJECT;
		}
	}
