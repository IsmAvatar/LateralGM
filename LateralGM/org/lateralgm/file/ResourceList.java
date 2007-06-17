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

	ResourceList(Class<R> type)
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

	// Be careful when using this with rooms (they default to LGM.currentFile as their owner)
	public R add()
		{
		R res = null;
		try
			{
			res = type.newInstance();
			res.setName(res.getName() + (lastId + 1));
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

	/** May return null */
	public R get(ResId id)
		{
		int listIndex = index(id);
		if (listIndex != -1) return resources.get(listIndex);
		return null;
		}

	/** May return null */
	public R get(String name)
		{
		int listIndex = index(name);
		if (listIndex != -1) return resources.get(listIndex);
		return null;
		}

	public R getList(int listIndex)
		{
		if (listIndex >= 0 && listIndex < resources.size()) return resources.get(listIndex);
		return null;
		}

	public void remove(ResId id)
		{
		int listIndex = index(id);
		if (listIndex != -1) remove(listIndex);
		}

	public void remove(String name)
		{
		int listIndex = index(name);
		if (listIndex != -1) remove(listIndex);
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

	public int index(String name)
		{
		for (int i = 0; i < resources.size(); i++)
			{
			if (resources.get(i).getName().equals(name))
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
		if (res != null)
			{
			if (update)
				res2 = (R) res.copy(this);
			else
				res2 = (R) res.copy();
			}
		return res2;
		}

	public void replace(ResId srcId, R replacement)
		{
		int ind = index(srcId);
		replace(ind,replacement);
		fireStateChanged();
		}

	public void replace(int srcIndex, R replacement)
		{
		if (srcIndex >= 0 && srcIndex < resources.size() && replacement != null)
			{
			resources.set(srcIndex,replacement);
			replacement.addChangeListener(rcl);
			fireStateChanged();
			}
		}

	public void defragIds()
		{
		sort();
		for (int i = 0; i < resources.size(); i++)
			{
			resources.get(i).getId().setValue(i);
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

	@SuppressWarnings("unchecked")
	public R[] toArray()
		{
		Resource[] result = new Resource[resources.size()];
		return (R[]) resources.toArray(result);
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
