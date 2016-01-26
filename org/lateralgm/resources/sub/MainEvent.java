/*
 * Copyright (C) 2006 Clam <clamisgood@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.util.ArrayList;
import java.util.List;

import org.lateralgm.main.Util;
import org.lateralgm.main.Util.InherentlyUnique;

public class MainEvent implements InherentlyUnique<MainEvent>
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
	public static final byte EV_TRIGGER = 11;

	public List<Event> events = new ArrayList<Event>();

	public boolean isEqual(MainEvent other)
		{
		if (this == other) return true;
		if (other == null) return false;
		return Util.areInherentlyUniquesEqual(events,other.events);
		}
	}
