/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * 
 * Lateral GM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Lateral GM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lateral GM; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.lateralgm.resources;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.file.ResourceList;
import org.lateralgm.main.LGM;

//TODO Implement Resource.equals method
public abstract class Resource implements Comparable<Resource>
	{
	public static final byte SPRITE = 2;
	public static final byte SOUND = 3;
	public static final byte BACKGROUND = 6;
	public static final byte PATH = 8;
	public static final byte SCRIPT = 7;
	public static final byte FONT = 9;
	public static final byte TIMELINE = 12;
	public static final byte GMOBJECT = 1;
	public static final byte ROOM = 4;

	public static final byte GAMEINFO = 10;
	public static final byte GAMESETTINGS = 11;
	public static final byte EXTENSIONS = 13;

	public static final ImageIcon[] ICON = new ImageIcon[14];
	static
		{
		ICON[SPRITE] = LGM.getIconForKey("Resource.SPRITE");
		ICON[SOUND] = LGM.getIconForKey("Resource.SOUND");
		ICON[BACKGROUND] = LGM.getIconForKey("Resource.BACKGROUND");
		ICON[PATH] = LGM.getIconForKey("Resource.PATH");
		ICON[SCRIPT] = LGM.getIconForKey("Resource.SCRIPT");
		ICON[FONT] = LGM.getIconForKey("Resource.FONT");
		ICON[TIMELINE] = LGM.getIconForKey("Resource.TIMELINE");
		ICON[GMOBJECT] = LGM.getIconForKey("Resource.GMOBJECT");
		ICON[ROOM] = LGM.getIconForKey("Resource.ROOM");
		ICON[GAMEINFO] = LGM.getIconForKey("Resource.GAMEINFO");
		ICON[GAMESETTINGS] = LGM.getIconForKey("Resource.GAMESETTINGS");
		ICON[EXTENSIONS] = GmTreeGraphics.getBlankIcon();
		}

	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;

	private ResId id = new ResId(0);
	private String name = "";

	public int compareTo(Resource res)
		{
		return res.id.getValue() == id.getValue() ? 0 : (res.id.getValue() < id.getValue() ? -1 : 1);
		}

	public void addChangeListener(ChangeListener l)
		{
		listenerList.add(ChangeListener.class,l);
		}

	public void removeChangeListener(ChangeListener l)
		{
		listenerList.remove(ChangeListener.class,l);
		}

	protected void fireStateChanged()
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ChangeListener.class)
				{
				// Lazily create the event:
				if (changeEvent == null) changeEvent = new ChangeEvent(this);
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
				}
			}
		}

	public ResId getId()
		{
		return id;
		}

	public void setId(ResId id)
		{
		this.id = id;
		fireStateChanged();
		}

	public String getName()
		{
		return name;
		}

	public void setName(String name)
		{
		this.name = name;
		fireStateChanged();
		}

	@SuppressWarnings("unchecked")
	public abstract Resource copy(ResourceList src);
	
	public abstract Resource copy();

	public abstract byte getKind();

	public String toString()
		{
		return name;
		}
	}
