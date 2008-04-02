/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.event.KeyEvent;

import javax.swing.JTextField;

import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.Event;
import org.lateralgm.resources.sub.MainEvent;
import org.lateralgm.subframes.EventFrame;

public class EventKeyInput extends JTextField
	{
	private static final long serialVersionUID = 1L;

	public int selectedKey;
	public EventFrame parent;

	public EventKeyInput(EventFrame parent)
		{
		this.parent = parent;
		setFocusTraversalKeysEnabled(false);
		setDragEnabled(false);
		}

	public void processKeyEvent(KeyEvent e)
		{
		if (e.getID() == KeyEvent.KEY_PRESSED)
			{
			int key = Event.getGmKeyCode(e.getKeyCode());
			if (Event.KEYS.contains(key))
				{
				selectedKey = key;
				setText(Event.getGmKeyName(selectedKey));
				if (parent.selectedNode != null) switch (parent.selectedNode.mainId)
					{
					case MainEvent.EV_KEYBOARD:
					case MainEvent.EV_KEYPRESS:
					case MainEvent.EV_KEYRELEASE:
						parent.selectedNode.eventId = selectedKey;
						break;
					default:
					}
				}
			else if (key == Prefs.eventKeyInputAddKey)
				{
				if (parent.linkedFrame != null && Event.KEYS.contains(selectedKey))
					{
					parent.linkedFrame.addEvent(new Event(parent.selectedNode.mainId,selectedKey));
					}
				}
			}
		}
	}
