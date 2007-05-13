/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.util.ArrayList;
import java.util.Collections;

import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class ResourceList<R extends Resource>
	{
	private ArrayList<R> resources = new ArrayList<R>();

	private Class<R> type; // used as a workaround for add()

	private final ResourceChangeListener rcl = new ResourceChangeListener();

	EventListenerList listenerList = new EventListenerList();
	ChangeEvent changeEvent = null;

	ResourceList(Class<R> type) // it's *YOUR* problem if this class doesn't extend Resource (you shouldn't really
	// need to construct a ResourceList manually anyway)
		{
		this.type = type;
		}

	public int lastId = -1;

	public int count()
		{
		return resources.size();
		}

	public R add(R res)
		{
		resources.add(res);
		res.setId(new ResId(++lastId));
		res.addChangeListener(rcl);
		fireStateChanged();
		return res;
		}

	public R add()// Be careful when using this with rooms (they default to LGM.currentFile as their owner)
		{
		R res = null;
		try
			{
			res = type.newInstance();
			res.setName(res.getName() + lastId);
			add(res);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		return res;
		}

	public R getUnsafe(int id)
		{
		for (int i = 0; i < resources.size(); i++)
			{
			if (resources.get(i).getId().getValue() == id)
				{
				return resources.get(i);
				}
			}
		return null;
		}

	public R get(ResId id)
		{
		int ListIndex = index(id);
		if (ListIndex != -1) return resources.get(ListIndex);
		return null;
		}

	public R get(String Name)
		{
		int ListIndex = index(Name);
		if (ListIndex != -1) return resources.get(ListIndex);
		return null;
		}

	public R getList(int ListIndex)
		{
		if (ListIndex >= 0 && ListIndex < resources.size()) return resources.get(ListIndex);
		return null;
		}

	public void remove(ResId id)
		{
		int ListIndex = index(id);
		if (ListIndex != -1) remove(ListIndex);
		}

	public void remove(String Name)
		{
		int ListIndex = index(Name);
		if (ListIndex != -1) remove(ListIndex);
		}
	
	public void remove(int index)
		{
		resources.get(index).removeChangeListener(rcl);
		resources.remove(index);
		fireStateChanged();
		}

	public int index(ResId id)
		{
		for (int i = 0; i < resources.size(); i++)
			{
			if (resources.get(i).getId() == id)
				{
				return i;
				}
			}
		return -1;
		}

	public int index(String Name)
		{
		for (int i = 0; i < resources.size(); i++)
			{
			if (resources.get(i).getName().equals(Name))
				{
				return i;
				}
			}
		return -1;
		}

	public void clear()
		{
		if (resources.size() == 0) return;
		for (R r : resources)
			{
			r.removeChangeListener(rcl);
			}
		resources.clear();
		fireStateChanged();
		}

	public void sort()
		{
		Collections.sort(resources);
		}

	@SuppressWarnings("unchecked")
	public R duplicate(ResId id, boolean update)
		{
		R res = get(id);
		R res2 = null;
		if (res != null) res2 = (R) res.copy(update,this);
		return res2;
		}

	public void replace(ResId srcId, R replacement)
		{
		int ind = index(srcId);
		replace(ind, replacement);
		fireStateChanged();
		}

	public void replace(int SrcIndex, R Replacement)
		{
		if (SrcIndex >= 0 && SrcIndex < resources.size() && Replacement != null)
			{
			resources.set(SrcIndex,Replacement);
			Replacement.addChangeListener(rcl);
			fireStateChanged();
			}
		}

	public void defragIds()
		{
		sort();
		for (int i = 0; i < resources.size(); i++)
			{
			resources.get(i).setId(new ResId(i));
			}
		lastId = resources.size() - 1;
		}

	public void addChangeListener(ChangeListener l)
		{
		listenerList.add(ChangeListener.class,l);
		}

	public void removeChangeListener(ChangeListener l)
		{
		listenerList.remove(ChangeListener.class,l);
		}

	protected void fireStateChanged(ChangeEvent e)
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ChangeListener.class)
				{
				((ChangeListener) listeners[i + 1]).stateChanged(e);
				}
			}
		}

	protected void fireStateChanged()
		{
		// Lazily create the event:
		if (changeEvent == null) changeEvent = new ChangeEvent(this);
		fireStateChanged(changeEvent);
		}

	private class ResourceChangeListener implements ChangeListener
		{
		public void stateChanged(ChangeEvent e)
			{
			fireStateChanged(e);
			}
		}
	}