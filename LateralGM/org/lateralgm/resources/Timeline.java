/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Argument;
import org.lateralgm.resources.sub.Moment;

public class Timeline extends Resource
	{
	public ArrayList<Moment> moments = new ArrayList<Moment>();

	public Timeline()
		{
		setName(Prefs.prefixes[Resource.TIMELINE]);
		}

	public Moment addMoment()
		{
		Moment m = new Moment();
		moments.add(m);
		return m;
		}

	public Moment getMoment(int stepNo)
		{
		for (Moment m : moments)
			if (m.stepNo == stepNo)
				return m;
		return null;
		}

	@SuppressWarnings("unchecked")
	public Timeline copy(boolean update, ResourceList src)
		{
		Timeline time = new Timeline();
		for (Moment mom : moments)
			{
			Moment mom2 = time.addMoment();
			mom2.stepNo = mom.stepNo;
			for (Action act : mom.actions)
				{
				Action act2 = mom2.addAction();
				act2.relative = act.relative;
				act2.not = act.not;
				act2.appliesTo = act.appliesTo;
				act2.arguments = new Argument[act.arguments.length];
				for (int k = 0; k < act.arguments.length; k++)
					act2.arguments[k] = new Argument(act.arguments[k].kind,act.arguments[k].val,
							act.arguments[k].res);
				}
			}
		if (update)
			{
			time.setId(new ResId(++src.lastId));
			time.setName(Prefs.prefixes[Resource.TIMELINE] + src.lastId);
			src.add(time);
			}
		else
			{
			time.setId(getId());
			time.setName(getName());
			}
		return time;
		}
	}
