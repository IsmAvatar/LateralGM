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
	private ArrayList<Moment> moments = new ArrayList<Moment>();

	public Timeline()
		{
		setName(Prefs.prefixes[Resource.TIMELINE]);
		}

	public int NoMoments()
		{
		return moments.size();
		}

	public Moment addMoment()
		{
		moments.add(new Moment());
		return moments.get(NoMoments() - 1);
		}

	public Moment getMoment(int stepNo)
		{
		int ListIndex = MomentIndex(stepNo);
		if (ListIndex != -1) return moments.get(ListIndex);
		return null;
		}

	public Moment getMomentList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoMoments()) return moments.get(ListIndex);
		return null;
		}

	public void removeMoment(int MomentVal)
		{
		int ListIndex = MomentIndex(MomentVal);
		if (ListIndex != -1) moments.remove(ListIndex);
		}

	public int MomentIndex(int stepNo)
		{
		for (int i = 0; i < NoMoments(); i++)
			{
			if (getMomentList(i).stepNo == stepNo)
				{
				return i;
				}
			}
		return -1;
		}

	public void clearMoments()
		{
		moments.clear();
		}

	@SuppressWarnings("unchecked")
	public Timeline copy(boolean update, ResourceList src)
		{
		Timeline time = new Timeline();
		for (int i = 0; i < NoMoments(); i++)
			{
			Moment mom = getMomentList(i);
			Moment mom2 = time.addMoment();
			mom2.stepNo = mom.stepNo;
			for (int j = 0; j < mom.NoActions(); j++)
				{
				Action act = mom.getAction(j);
				Action act2 = mom2.addAction();
				act2.relative = act.relative;
				act2.not = act.not;
				act2.appliesTo = act.appliesTo;
				act2.arguments = new Argument[act.arguments.length];
				for (int k = 0; k < act.arguments.length; k++)
					act2.arguments[k] = new Argument(act.arguments[k].kind,act.arguments[k].val,act.arguments[k].res);
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