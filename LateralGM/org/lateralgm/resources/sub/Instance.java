/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.lateralgm.resources.GmObject;

public class Instance
	{
	private static final long serialVersionUID = 1L;

	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;

	private int x = 0;
	private int y = 0;
	public WeakReference<GmObject> gmObjectId = null;
	public int instanceId = 0;
	private String creationCode = "";
	public boolean locked = false;

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

	public int getX()
		{
		return x;
		}

	public void setX(int x)
		{
		this.x = x;
		fireStateChanged();
		}

	public int getY()
		{
		return y;
		}

	public void setY(int y)
		{
		this.y = y;
		fireStateChanged();
		}

	public String getCreationCode()
		{
		return creationCode;
		}

	public void setCreationCode(String creationCode)
		{
		this.creationCode = creationCode;
		fireStateChanged();
		}
	}
