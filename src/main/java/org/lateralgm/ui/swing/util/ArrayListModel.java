/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.util;

import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.util.ActiveArrayList;
import org.lateralgm.util.ActiveArrayList.ListUpdateEvent;

public class ArrayListModel<E> implements ListModel<E>,UpdateListener
	{
	ActiveArrayList<E> list;
	ArrayList<ListDataListener> listeners;

	public ArrayListModel(ActiveArrayList<E> l)
		{
		list = l;
		l.updateSource.addListener(this);
		listeners = new ArrayList<ListDataListener>();
		}

	public void addListDataListener(ListDataListener l)
		{
		listeners.add(l);
		}

	public E getElementAt(int index)
		{
		try
			{
			return list.get(index);
			}
		catch (IndexOutOfBoundsException e)
			{
			return null;
			}
		}

	public int getSize()
		{
		return list.size();
		}

	public void removeListDataListener(ListDataListener l)
		{
		listeners.remove(l);
		}

	public void updated(UpdateEvent e)
		{
		ListDataEvent lde;
		if (e instanceof ListUpdateEvent)
			{
			ListUpdateEvent lue = (ListUpdateEvent) e;
			int t;
			switch (lue.type)
				{
				case ADDED:
					t = ListDataEvent.INTERVAL_ADDED;
					break;
				case REMOVED:
					t = ListDataEvent.INTERVAL_REMOVED;
					break;
				case CHANGED:
					t = ListDataEvent.CONTENTS_CHANGED;
					break;
				default:
					throw new AssertionError();
				}
			lde = new ListDataEvent(e.source.owner,t,lue.fromIndex,lue.toIndex);
			}
		else
			lde = new ListDataEvent(e.source.owner,ListDataEvent.CONTENTS_CHANGED,0,Integer.MAX_VALUE);
		for (ListDataListener l : listeners)
			l.contentsChanged(lde);
		}
	}
