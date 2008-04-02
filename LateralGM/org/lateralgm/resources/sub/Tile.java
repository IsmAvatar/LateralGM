/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.lateralgm.resources.Background;

public class Tile
	{
	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;
	private int x = 0;
	private int y = 0;
	private WeakReference<Background> backgroundId = null;
	private int tileX = 0;
	private int tileY = 0;
	private int width = 16;
	private int height = 16;
	private int depth = 0;
	public int tileId = 0;
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

	public WeakReference<Background> getBackgroundId()
		{
		return backgroundId;
		}

	public void setBackgroundId(WeakReference<Background> backgroundId)
		{
		this.backgroundId = backgroundId;
		fireStateChanged();
		}

	public int getTileX()
		{
		return tileX;
		}

	public void setTileX(int tileX)
		{
		this.tileX = tileX;
		fireStateChanged();
		}

	public int getTileY()
		{
		return tileY;
		}

	public void setTileY(int tileY)
		{
		this.tileY = tileY;
		fireStateChanged();
		}

	public int getWidth()
		{
		return width;
		}

	public void setWidth(int width)
		{
		this.width = width;
		fireStateChanged();
		}

	public int getHeight()
		{
		return height;
		}

	public void setHeight(int height)
		{
		this.height = height;
		fireStateChanged();
		}

	public int getDepth()
		{
		return depth;
		}

	public void setDepth(int depth)
		{
		this.depth = depth;
		fireStateChanged();
		}
	}
