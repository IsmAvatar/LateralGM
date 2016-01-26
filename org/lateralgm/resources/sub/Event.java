/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import static org.lateralgm.main.Util.deRef;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.lateralgm.main.Util;
import org.lateralgm.main.Util.InherentlyUnique;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;

public class Event extends ActionContainer implements Comparable<Event>,InherentlyUnique<Event>
	{
	// mouse event types
	public static final byte EV_LEFT_BUTTON = 0;
	public static final byte EV_RIGHT_BUTTON = 1;
	public static final byte EV_MIDDLE_BUTTON = 2;
	public static final byte EV_NO_BUTTON = 3;
	public static final byte EV_LEFT_PRESS = 4;
	public static final byte EV_RIGHT_PRESS = 5;
	public static final byte EV_MIDDLE_PRESS = 6;
	public static final byte EV_LEFT_RELEASE = 7;
	public static final byte EV_RIGHT_RELEASE = 8;
	public static final byte EV_MIDDLE_RELEASE = 9;
	public static final byte EV_MOUSE_ENTER = 10;
	public static final byte EV_MOUSE_LEAVE = 11;
	public static final byte EV_MOUSE_WHEEL_UP = 60;
	public static final byte EV_MOUSE_WHEEL_DOWN = 61;
	public static final byte EV_GLOBAL_LEFT_BUTTON = 50;
	public static final byte EV_GLOBAL_RIGHT_BUTTON = 51;
	public static final byte EV_GLOBAL_MIDDLE_BUTTON = 52;
	public static final byte EV_GLOBAL_LEFT_PRESS = 53;
	public static final byte EV_GLOBAL_RIGHT_PRESS = 54;
	public static final byte EV_GLOBAL_MIDDLE_PRESS = 55;
	public static final byte EV_GLOBAL_LEFT_RELEASE = 56;
	public static final byte EV_GLOBAL_RIGHT_RELEASE = 57;
	public static final byte EV_GLOBAL_MIDDLE_RELEASE = 58;
	public static final byte EV_JOYSTICK1_LEFT = 16;
	public static final byte EV_JOYSTICK1_RIGHT = 17;
	public static final byte EV_JOYSTICK1_UP = 18;
	public static final byte EV_JOYSTICK1_DOWN = 19;
	public static final byte EV_JOYSTICK1_BUTTON1 = 21;
	public static final byte EV_JOYSTICK1_BUTTON2 = 22;
	public static final byte EV_JOYSTICK1_BUTTON3 = 23;
	public static final byte EV_JOYSTICK1_BUTTON4 = 24;
	public static final byte EV_JOYSTICK1_BUTTON5 = 25;
	public static final byte EV_JOYSTICK1_BUTTON6 = 26;
	public static final byte EV_JOYSTICK1_BUTTON7 = 27;
	public static final byte EV_JOYSTICK1_BUTTON8 = 28;
	public static final byte EV_JOYSTICK2_LEFT = 31;
	public static final byte EV_JOYSTICK2_RIGHT = 32;
	public static final byte EV_JOYSTICK2_UP = 33;
	public static final byte EV_JOYSTICK2_DOWN = 34;
	public static final byte EV_JOYSTICK2_BUTTON1 = 36;
	public static final byte EV_JOYSTICK2_BUTTON2 = 37;
	public static final byte EV_JOYSTICK2_BUTTON3 = 38;
	public static final byte EV_JOYSTICK2_BUTTON4 = 39;
	public static final byte EV_JOYSTICK2_BUTTON5 = 40;
	public static final byte EV_JOYSTICK2_BUTTON6 = 41;
	public static final byte EV_JOYSTICK2_BUTTON7 = 42;
	public static final byte EV_JOYSTICK2_BUTTON8 = 43;

	// keyboard events
	public static final byte EV_NO_KEY = 0;
	public static final byte EV_ANY_KEY = 1;
	public static final byte EV_ENTER_KEY = 13;
	public static final byte EV_DELETE_KEY = 46;
	public static final byte EV_INSERT_KEY = 45;

	// other event types
	public static final byte EV_OUTSIDE = 0;
	public static final byte EV_BOUNDARY = 1;
	public static final byte EV_GAME_START = 2;
	public static final byte EV_GAME_END = 3;
	public static final byte EV_ROOM_START = 4;
	public static final byte EV_ROOM_END = 5;
	public static final byte EV_NO_MORE_LIVES = 6;
	public static final byte EV_NO_MORE_HEALTH = 9;
	public static final byte EV_ANIMATION_END = 7;
	public static final byte EV_END_OF_PATH = 8;
	public static final byte EV_USER0 = 10;
	public static final byte EV_USER1 = 11;
	public static final byte EV_USER2 = 12;
	public static final byte EV_USER3 = 13;
	public static final byte EV_USER4 = 14;
	public static final byte EV_USER5 = 15;
	public static final byte EV_USER6 = 16;
	public static final byte EV_USER7 = 17;
	public static final byte EV_USER8 = 18;
	public static final byte EV_USER9 = 19;
	public static final byte EV_USER10 = 20;
	public static final byte EV_USER11 = 21;
	public static final byte EV_USER12 = 22;
	public static final byte EV_USER13 = 23;
	public static final byte EV_USER14 = 24;
	public static final byte EV_USER15 = 25;
	public static final byte EV_OUTSIDEVIEW0 = 40;
	public static final byte EV_OUTSIDEVIEW1 = 41;
	public static final byte EV_OUTSIDEVIEW2 = 42;
	public static final byte EV_OUTSIDEVIEW3 = 43;
	public static final byte EV_OUTSIDEVIEW4 = 44;
	public static final byte EV_OUTSIDEVIEW5 = 45;
	public static final byte EV_OUTSIDEVIEW6 = 46;
	public static final byte EV_OUTSIDEVIEW7 = 47;
	public static final byte EV_BOUNDARYVIEW0 = 50;
	public static final byte EV_BOUNDARYVIEW1 = 51;
	public static final byte EV_BOUNDARYVIEW2 = 52;
	public static final byte EV_BOUNDARYVIEW3 = 53;
	public static final byte EV_BOUNDARYVIEW4 = 54;
	public static final byte EV_BOUNDARYVIEW5 = 55;
	public static final byte EV_BOUNDARYVIEW6 = 56;
	public static final byte EV_BOUNDARYVIEW7 = 57;
	public static final byte EV_CLOSEWINDOW = 30;
	public static final byte EV_IMAGELOADED = 60;
	public static final byte EV_SOUNDLOADED = 61;
	public static final byte EV_HTTP = 62;
	public static final byte EV_DIALOG = 63;
	public static final byte EV_IAP = 66;
	public static final byte EV_CLOUD = 67;
	public static final byte EV_NETWORKING = 68;
	public static final byte EV_STEAM = 69;
	public static final byte EV_SOCIAL = 70;

	// step event types
	public static final byte EV_STEP_NORMAL = 0;
	public static final byte EV_STEP_BEGIN = 1;
	public static final byte EV_STEP_END = 2;

	//alarm event types
	public static final byte EV_ALARM0 = 0;
	public static final byte EV_ALARM1 = 1;
	public static final byte EV_ALARM2 = 2;
	public static final byte EV_ALARM3 = 3;
	public static final byte EV_ALARM4 = 4;
	public static final byte EV_ALARM5 = 5;
	public static final byte EV_ALARM6 = 6;
	public static final byte EV_ALARM7 = 7;
	public static final byte EV_ALARM8 = 8;
	public static final byte EV_ALARM9 = 9;
	public static final byte EV_ALARM10 = 10;
	public static final byte EV_ALARM11 = 11;

	// draw event types
	public static final byte EV_DRAW_NORMAL = 0;
	// for whatever dumbfuck reason the number jumps to 64 with GMX
	public static final byte EV_DRAW_GUI = 64;
	/*
	 * I seriously can not believe that they make this a fucking draw event
	 * especially when the dumbasses seriously, I am so sick of their shit
	 * they had fucking game window closed event, why didn't they just stick
	 * it with that in a new group under 7_30X for other events. Stupid
	 * dumbasses removing functionality and replacing it with... just, somebody,
	 * please shoot me.
	 */
	public static final byte EV_DRAW_RESIZE = 65;

	/*
	 * note: The constant definitions were generated by a small GM program, which parses
	 * the clipboard (with parts of the gm manual on it) and generates the Java definitions.
	 */

	public int id = 0;
	public ResourceReference<GmObject> other = null; // For collision Events
	public int mainId = 0;

	/** Left index shall be Java Key, right index shall be GM Key. */
	public static final Map<Integer,Integer> KEYS;

	public Event()
		{
		}

	public Event(int mainId, int id)
		{
		this.mainId = mainId;
		this.id = id;
		}

	public Event(int mainId, ResourceReference<GmObject> other)
		{
		this.mainId = mainId;
		this.other = other;
		}

	public Event(int mainId, int id, ResourceReference<GmObject> other)
		{
		this.mainId = mainId;
		this.id = id;
		this.other = other;
		}

	public String toString()
		{
		switch (mainId)
			{
			case MainEvent.EV_COLLISION:
				GmObject obj = deRef(other);
				String name;
				if (obj == null)
					name = "<undefined>";
				else
					name = obj.getName();
				return Messages.format("Event.EVENT4_X",name); //$NON-NLS-1$
			default:
				return eventName(mainId,id);
			}
		}

	public int compareTo(Event e)
		{
		if (e.mainId != mainId) return e.mainId > mainId ? -1 : 1;
		return e.id > id ? -1 : e.id == id ? 0 : 1;
		}

	public boolean matchesType(Event e)
		{
		if (e.mainId != mainId) return false;
		if (mainId != MainEvent.EV_COLLISION) return e.id == id;
		return e.other == other;
		}

	//TODO: Move all of these keys to the new keyboard properties map.
	public static String getGmKeyName(int keyCode)
		{
		switch (keyCode)
			{
			case 0:
				return Messages.getString("Event.NO_KEY");
			case 1:
				return Messages.getString("Event.ANY_KEY");
			case 13:
				keyCode = KeyEvent.VK_ENTER;
				break;
			case 46:
				keyCode = KeyEvent.VK_DELETE;
				break;
			case 45:
				keyCode = KeyEvent.VK_INSERT;
				break;
			default:
				break;
			}
		return KeyEvent.getKeyText(keyCode);
		}

	/*	public static int getGmKeyCode(int keyCode)
			{
			switch (keyCode)
				{
				case KeyEvent.VK_ENTER:
					return 13;
				case KeyEvent.VK_DELETE:
					return 46;
				case KeyEvent.VK_INSERT:
					return 45;
				default:
					return keyCode;
				}
			}*/

	public static String eventName(int mainId, int eventId)
		{
		switch (mainId)
			{
			case MainEvent.EV_ALARM:
			case MainEvent.EV_TRIGGER:
				return Messages.format("Event.EVENT" + mainId + "_X",eventId); //$NON-NLS-1$
			case MainEvent.EV_MOUSE:
				if ((eventId <= 9) || (eventId >= 50 && eventId <= 58))
					{
					int i = eventId % 10;
					int b;
					boolean g = eventId >= 50;
					if (g || i < 3)
						b = i % 3;
					else if (i > 3)
						b = --i % 3;
					else
						{
						b = -1;
						i = 0;
						}
					return Messages.format("Event.EVENT6_BUTTON",b,i / 3,g ? 1 : 0);
					}
				else if ((eventId >= 16 && eventId <= 28) || (eventId >= 31 && eventId <= 43))
					{
					int j = eventId < 31 ? 1 : 2;
					int i = eventId - (j == 1 ? 16 : 31);
					return Messages.format("Event.EVENT6_JOYSTICK",j,i < 4 ? i : 4,i - 4);
					}
				return Messages.getString("Event.EVENT" + mainId + "_" + eventId); //$NON-NLS-1$
			case MainEvent.EV_KEYBOARD:
			case MainEvent.EV_KEYPRESS:
			case MainEvent.EV_KEYRELEASE:
				return Messages.format("Event.EVENT" + mainId + "_X",getGmKeyName(eventId)); //$NON-NLS-1$
			case MainEvent.EV_OTHER:
				if (eventId >= EV_USER0 && eventId <= EV_USER15)
					return Messages.format("Event.EVENT" + mainId + "_X",eventId - EV_USER0); //$NON-NLS-1$
				if (eventId >= EV_OUTSIDEVIEW0 && eventId <= EV_OUTSIDEVIEW7)
					return Messages.format("Event.EVENT" + mainId + "_40X",eventId - EV_OUTSIDEVIEW0); //$NON-NLS-1$
				if (eventId >= EV_BOUNDARYVIEW0 && eventId <= EV_BOUNDARYVIEW7)
					return Messages.format("Event.EVENT" + mainId + "_50X",eventId - EV_BOUNDARYVIEW0); //$NON-NLS-1$
				return Messages.getString("Event.EVENT" + mainId + "_" + eventId); //$NON-NLS-1$
			default:
				return Messages.getString("Event.EVENT" + mainId + "_" + eventId); //$NON-NLS-1$
			}
		}

	public Event copy()
		{
		Event ev = new Event();
		ev.mainId = mainId;
		ev.id = id;
		ev.other = other;
		for (Action act : actions)
			ev.actions.add(act.copy());
		return ev;
		}

	static
		{
		Map<Integer,Integer> keys = new HashMap<Integer,Integer>();

		keys.put(KeyEvent.VK_LEFT,37);
		keys.put(KeyEvent.VK_RIGHT,39);
		keys.put(KeyEvent.VK_UP,38);
		keys.put(KeyEvent.VK_DOWN,40);

		keys.put(KeyEvent.VK_CONTROL,17); //vk_control
		keys.put(KeyEvent.VK_ALT,18); //vk_alt
		keys.put(KeyEvent.VK_SHIFT,16); //vk_shift
		keys.put(KeyEvent.VK_SPACE,32); //vk_space
		keys.put((int) Event.EV_ENTER_KEY,13); //vk_enter

		//Numpad happens to map 1:1 (96..105)
		for (int c = KeyEvent.VK_NUMPAD0; c <= KeyEvent.VK_NUMPAD9; c++)
			keys.put(c,c);
		/*		keys.put(KeyEvent.VK_NUMPAD0,96); //vk_numpad0
				keys.put(KeyEvent.VK_97); //vk_numpad1
				keys.put(KeyEvent.VK_98); //vk_numpad2
				keys.put(KeyEvent.VK_99); //vk_numpad3
				keys.put(KeyEvent.VK_100); //vk_numpad4
				keys.put(KeyEvent.VK_101); //vk_numpad5
				keys.put(KeyEvent.VK_102); //vk_numpad6
				keys.put(KeyEvent.VK_103); //vk_numpad7
				keys.put(KeyEvent.VK_104); //vk_numpad8
				keys.put(KeyEvent.VK_105); //vk_numpad9
		*/
		keys.put(KeyEvent.VK_DIVIDE,111); //vk_divide
		keys.put(KeyEvent.VK_MULTIPLY,106); //vk_multiply
		keys.put(KeyEvent.VK_SUBTRACT,109); //vk_subtract
		keys.put(KeyEvent.VK_ADD,107); //vk_add
		keys.put(KeyEvent.VK_DECIMAL,110); //vk_decimal

		//VK_0-9 and VK_A-Z are the same as ASCII
		for (int c = '0'; c <= '9'; c++)
			keys.put(c,c);

		for (int c = 'A'; c <= 'Z'; c++)
			keys.put(c,c);

		//F1-12 happen to map 1:1 (112..123)
		for (int c = KeyEvent.VK_F1; c <= KeyEvent.VK_F12; c++)
			keys.put(c,c);
		/*		keys.put(KeyEvent.VK_F1,112); //vk_f1
				keys.put(KeyEvent.VK_113); //vk_f2
				keys.put(KeyEvent.VK_114); //vk_f3
				keys.put(KeyEvent.VK_115); //vk_f4
				keys.put(KeyEvent.VK_116); //vk_f5
				keys.put(KeyEvent.VK_117); //vk_f6
				keys.put(KeyEvent.VK_118); //vk_f7
				keys.put(KeyEvent.VK_119); //vk_f8
				keys.put(KeyEvent.VK_120); //vk_f9
				keys.put(KeyEvent.VK_121); //vk_f10
				keys.put(KeyEvent.VK_122); //vk_f11
				keys.put(KeyEvent.VK_123); //vk_f12
		*/
		keys.put(KeyEvent.VK_BACK_SPACE,8); //vk_backspace
		keys.put(KeyEvent.VK_ESCAPE,27); //vk_escape
		keys.put(KeyEvent.VK_HOME,36); //vk_home
		keys.put(KeyEvent.VK_END,35); //vk_end
		keys.put(KeyEvent.VK_PAGE_UP,33); //vk_pageup
		keys.put(KeyEvent.VK_PAGE_DOWN,34); //vk_pagedown
		keys.put((int) Event.EV_DELETE_KEY,46); //vk_delete
		keys.put((int) Event.EV_INSERT_KEY,45); //vk_insert

		keys.put((int) Event.EV_NO_KEY,0); //vk_nokey
		keys.put((int) Event.EV_ANY_KEY,1); //vk_anykey
		KEYS = Collections.unmodifiableMap((keys));
		}

	public boolean isEqual(Event evt)
		{
		if (this == evt) return true;
		if (evt == null || id != evt.id || mainId != evt.mainId) return false;
		if (!Util.areInherentlyUniquesEqual(actions,evt.actions)) return false;
		if (other == null) return evt.other == null;
		return other.equals(evt.other);
		}
	}
