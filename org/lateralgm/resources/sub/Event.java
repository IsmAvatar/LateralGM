/*
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2008, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import static org.lateralgm.main.Util.deRef;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.ResourceReference;

public class Event extends ActionContainer implements Comparable<Event>
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

	/*
	 * note: The constant definitions were generated by a small GM program, which parses
	 * the clipboard (with parts of the gm manual on it) and generates the Java definitions.
	 */

	public int id = 0;
	public ResourceReference<GmObject> other = null; // For collision Events
	public int mainId = 0;

	public static final List<Integer> KEYS;

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

	public static int getGmKeyCode(int keyCode)
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
		}

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
				if (eventId >= EV_USER0)
					return Messages.format("Event.EVENT" + mainId + "_X",eventId - EV_USER0); //$NON-NLS-1$
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
		List<Integer> keys = new ArrayList<Integer>();

		keys.add(37); //vk_left
		keys.add(39); //vk_right
		keys.add(38); //vk_up
		keys.add(40); //vk_down

		keys.add(17); //vk_control
		keys.add(18); //vk_alt
		keys.add(16); //vk_shift
		keys.add(32); //vk_space
		keys.add(13); //vk_enter

		keys.add(96); //vk_numpad0
		keys.add(97); //vk_numpad1
		keys.add(98); //vk_numpad2
		keys.add(99); //vk_numpad3
		keys.add(100); //vk_numpad4
		keys.add(101); //vk_numpad5
		keys.add(102); //vk_numpad6
		keys.add(103); //vk_numpad7
		keys.add(104); //vk_numpad8
		keys.add(105); //vk_numpad9

		keys.add(111); //vk_divide
		keys.add(106); //vk_multiply
		keys.add(109); //vk_subtract
		keys.add(107); //vk_add		
		keys.add(110); //vk_decimal

		keys.add(48); //0
		keys.add(49); //1
		keys.add(50); //2
		keys.add(51); //3
		keys.add(52); //4
		keys.add(53); //5
		keys.add(54); //6
		keys.add(55); //7
		keys.add(56); //8
		keys.add(57); //9

		keys.add(65); //A
		keys.add(66); //B
		keys.add(67); //C
		keys.add(68); //D
		keys.add(69); //E
		keys.add(70); //F
		keys.add(71); //G
		keys.add(72); //H
		keys.add(73); //I
		keys.add(74); //J
		keys.add(75); //K
		keys.add(76); //L
		keys.add(77); //M
		keys.add(78); //N
		keys.add(79); //O
		keys.add(80); //P
		keys.add(81); //Q
		keys.add(82); //R
		keys.add(83); //S
		keys.add(84); //T
		keys.add(85); //U
		keys.add(86); //V
		keys.add(87); //W
		keys.add(88); //X
		keys.add(89); //Y
		keys.add(90); //Z

		keys.add(112); //vk_f1
		keys.add(113); //vk_f2
		keys.add(114); //vk_f3
		keys.add(115); //vk_f4
		keys.add(116); //vk_f5
		keys.add(117); //vk_f6
		keys.add(118); //vk_f7
		keys.add(119); //vk_f8
		keys.add(120); //vk_f9
		keys.add(121); //vk_f10
		keys.add(122); //vk_f11
		keys.add(123); //vk_f12

		keys.add(8); //vk_backspace
		keys.add(27); //vk_escape
		keys.add(36); //vk_home
		keys.add(35); //vk_end
		keys.add(33); //vk_pageup
		keys.add(34); //vk_pagedown	
		keys.add(46); //vk_delete
		keys.add(45); //vk_insert

		keys.add(0); //vk_nokey
		keys.add(1); //vk_anykey
		KEYS = Collections.unmodifiableList((keys));
		}
	}
