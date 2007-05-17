/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.util.ArrayList;

public class MainEvent
	{
	public static final byte EV_CREATE = 0;
	public static final byte EV_DESTROY = 1;
	public static final byte EV_ALARM = 2;
	public static final byte EV_STEP = 3;
	public static final byte EV_COLLISION = 4;
	public static final byte EV_KEYBOARD = 5;
	public static final byte EV_MOUSE = 6;
	public static final byte EV_OTHER = 7;
	public static final byte EV_DRAW = 8;
	public static final byte EV_KEYPRESS = 9;
	public static final byte EV_KEYRELEASE = 10;

	private ArrayList<Event> events = new ArrayList<Event>();

	public int noEvents()
		{
		return events.size();
		}

	public Event addEvent()
		{
		Event ev = new Event();
		events.add(ev);
		return ev;
		}

	public Event getEvent(int id)
		{
		int listIndex = eventIndex(id);
		if (listIndex != -1) return events.get(listIndex);
		return null;
		}

	public Event getEventList(int listIndex)
		{
		if (listIndex >= 0 && listIndex < noEvents()) return events.get(listIndex);
		return null;
		}

	public void removeEvent(int id)
		{
		int listIndex = eventIndex(id);
		if (listIndex != -1) events.remove(listIndex);
		}

	public int eventIndex(int id)
		{
		for (int i = 0; i < noEvents(); i++)
			{
			if (getEventList(i).id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public void clearEvents()
		{
		events.clear();
		}
	}
