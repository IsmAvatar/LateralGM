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

	private ArrayList<Event> Events = new ArrayList<Event>();

	public int NoEvents()
		{
		return Events.size();
		}

	public Event addEvent()
		{
		Event ev = new Event();
		Events.add(ev);
		return ev;
		}

	public Event getEvent(int id)
		{
		int ListIndex = EventIndex(id);
		if (ListIndex != -1) return Events.get(ListIndex);
		return null;
		}

	public Event getEventList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < NoEvents()) return Events.get(ListIndex);
		return null;
		}

	public void removeEvent(int id)
		{
		int ListIndex = EventIndex(id);
		if (ListIndex != -1) Events.remove(ListIndex);
		}

	public int EventIndex(int id)
		{
		for (int i = 0; i < NoEvents(); i++)
			{
			if (getEventList(i).Id == id)
				{
				return i;
				}
			}
		return -1;
		}

	public void clearEvents()
		{
		Events.clear();
		}
	}