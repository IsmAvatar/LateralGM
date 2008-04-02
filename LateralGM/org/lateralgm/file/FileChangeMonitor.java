/*
 * Copyright (C) 2008 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.io.File;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class FileChangeMonitor extends Thread
	{
	private static final int POLL_INTERVAL = 1000;

	public static final int FLAG_NONE = -1;
	public static final int FLAG_CHANGED = 0;
	public static final int FLAG_DELETED = 1;

	private File file;
	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;
	private int flag = FLAG_NONE;

	public FileChangeMonitor(File f)
		{
		if (!f.exists()) throw new IllegalArgumentException();
		file = f;
		}

	public FileChangeMonitor(String f)
		{
		this(new File(f));
		}

	public void run()
		{
		long lm = file.lastModified();
		try
			{
			while (true)
				{
				Thread.sleep(POLL_INTERVAL);
				if (!file.exists())
					{
					flag = FLAG_DELETED;
					System.out.println("deleted");
					fireStateChanged();
					break;
					}
				if (file.lastModified() != lm)
					{
					flag = FLAG_CHANGED;
					lm = file.lastModified();
					System.out.println("changed");
					fireStateChanged();
					}
				}
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		}

	public void addChangeListener(ChangeListener l)
		{
		listenerList.add(ChangeListener.class,l);
		}

	public void removeChangeListener(ChangeListener l)
		{
		listenerList.remove(ChangeListener.class,l);
		}

	public int getFlag()
		{
		return flag;
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
	}
