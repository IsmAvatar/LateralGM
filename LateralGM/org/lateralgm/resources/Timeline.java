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
import org.lateralgm.resources.sub.Moment;

public class Timeline extends Resource<Timeline>
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

	private Timeline copy(boolean update, ResourceList<Timeline> src)
		{
		Timeline time = new Timeline();
		for (Moment mom : moments)
			{
			Moment mom2 = mom.copy();
			time.moments.add(mom2);
			}
		if (update)
			{
			time.setName(Prefs.prefixes[Resource.TIMELINE] + (src.lastId + 1));
			src.add(time);
			}
		else
			{
			time.setId(getId());
			time.setName(getName());
			}
		return time;
		}

	public Timeline copy()
		{
		return copy(false,null);
		}

	public Timeline copy(ResourceList<Timeline> src)
		{
		return copy(true,src);
		}

	/**
	 * Performs a binary search through the list of moments for Moment with given Step Number
	 * @param k - Step Number to search for.
	 * @return Array index of Moment with given Step Number, or (-(position) - 1) if none found,
	 * where <i>position</i> is the index where a moment with given Step Number would be inserted.
	 */
	public int findMomentPosition(int k)
		{
		int low = 0;
		int high = moments.size() - 1;
		while (low <= high)
			{
			int mid = (low + high) >>> 1;
			if (moments.get(mid).stepNo < k)
				low = mid + 1;
			else if (moments.get(mid).stepNo > k)
				high = mid - 1;
			else
				return mid; // key found
			}
		return -(low + 1); // key not found
		}

	/**
	 * Shifts the Step Numbers of all moments in range (start,end) by given amount.
	 * In the event that start > end or amt == 0, no shift is performed.
	 * @param start - The smallest step number to shift
	 * @param end - The largest step number to shift
	 * @param amt - The amount to shift by
	 */
	public void shiftMoments(int start, int end, int amt)
		{
		if (start > end || amt == 0) return;
		int left = findMomentPosition(start);
		if (left >= moments.size()) return;
		if (left < 0) left = -left;
		else while (left >= 0 && moments.get(left).stepNo == start) left--; //handle duplicates
		for (int i = left; i < moments.size(); i++)
			{
			if (moments.get(i).stepNo > end) return;
			//TODO: Shift by amt AND maintain sorted order
			}
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
		int left = findMomentPosition(start);
		if (left >= moments.size()) return -1;
		if (left < 0) left = -left;
		else while (left > 0 && moments.get(left).stepNo == start) left--; //handle duplicates
		for (int i = left + 1; i < moments.size(); i++)
			{
			if (moments.get(i).stepNo > end) return left;
			moments.get(left).actions.addAll(moments.remove(i).actions);
			}
		return left;
		}

	public byte getKind()
		{
		return TIMELINE;
		}
	}
