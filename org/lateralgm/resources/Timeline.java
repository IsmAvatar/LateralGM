/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;
import java.util.Collections;

import org.lateralgm.resources.sub.Moment;
import org.lateralgm.util.PropertyMap;

public class Timeline extends InstantiableResource<Timeline,Timeline.PTimeline>
	{
	public ArrayList<Moment> moments = new ArrayList<Moment>();

	public enum PTimeline
		{ //Unused
		}

	public Timeline()
		{
		this(null);
		}

	public Timeline(ResourceReference<Timeline> r)
		{
		super(r);
		}

	public Timeline makeInstance(ResourceReference<Timeline> r)
		{
		return new Timeline(r);
		}

	public Moment addMoment()
		{
		Moment m = new Moment();
		moments.add(m);
		return m;
		}

	@Override
	protected void postCopy(Timeline dest)
		{
		super.postCopy(dest);
		for (Moment mom : moments)
			{
			Moment mom2 = mom.copy();
			dest.moments.add(mom2);
			}
		}

	/**
	 * Shifts the Step Numbers of all moments in range (start,end) by given amount.
	 * In the event that start > end or amt == 0, no shift is performed.
	 * @param start - The smallest step number to shift
	 * @param end - The largest step number to shift
	 * @param amt - The amount to shift by
	 * @return Original array index of first moment, or -1 if no shift was performed.
	 */
	public int shiftMoments(int start, int end, int amt)
		{
		if (start > end || amt == 0) return -1;
		int left = Collections.binarySearch(moments,start);
		if (left < 0)
			left = -left - 1;
		else
			while (left > 0 && moments.get(left).stepNo == start)
				left--; //handle duplicates
		for (int i = left; i < moments.size(); i++)
			{
			if (moments.get(i).stepNo > end) break;
			//This is not efficient, because it brings the list out of order.
			//Could have been dynamically sorted as stepNo was incremented.
			//Efficiency shouldn't matter much here, though, as this is infrequently called.
			moments.get(i).stepNo += amt;
			}
		Collections.sort(moments); //See efficiency comment above
		return left;
		}

	/**
	 * Merges all moments within the given range (start,end) into the first found moment
	 * by appending all actions in the order that they are found.
	 * @param start - The smallest step number to merge
	 * @param end - The largest step number to merge
	 * @return Array index of first moment, to which the other moments merged into,
	 * or -1 if no merge was performed.
	 */
	public int mergeMoments(int start, int end)
		{
		if (start > end) return -1;
		int left = Collections.binarySearch(moments,start);
		if (left < 0)
			left = -left - 1;
		else
			while (left > 0 && moments.get(left).stepNo == start)
				left--; //handle duplicates
		for (int i = left; i < moments.size(); i++)
			{
			if (moments.get(i).stepNo > end) return left;
			moments.get(left).actions.addAll(moments.remove(i).actions);
			}
		return left;
		}

	@Override
	protected PropertyMap<PTimeline> makePropertyMap()
		{
		return new PropertyMap<PTimeline>(PTimeline.class,this,null);
		}
	}
