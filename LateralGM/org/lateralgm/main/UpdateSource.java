/*
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.util.HashSet;
import java.util.WeakHashMap;

public class UpdateSource
	{
	public final Object owner;
	private final WeakHashMap<UpdateListener,Boolean> weakListeners;
	private WeakHashMap<UpdateListener,Boolean> wlAdd, wlRemove;
	private final HashSet<UpdateListener> hardListeners;
	private HashSet<UpdateListener> hlAdd, hlRemove;
	private int wlIterating, hlIterating;

	public UpdateSource(Object owner, UpdateTrigger t)
		{
		t.setSource(this);
		this.owner = owner;
		weakListeners = new WeakHashMap<UpdateListener,Boolean>();
		hardListeners = new HashSet<UpdateListener>();
		wlIterating = 0;
		hlIterating = 0;
		}

	public void addListener(UpdateListener l, boolean weak)
		{
		if (weak)
			{
			if (wlIterating > 0)
				{
				if (wlRemove != null) wlRemove.remove(l);
				if (!weakListeners.containsKey(l))
					{
					if (wlAdd == null) wlAdd = new WeakHashMap<UpdateListener,Boolean>();
					wlAdd.put(l,Boolean.TRUE);
					}
				}
			else
				weakListeners.put(l,Boolean.TRUE);
			}
		else
			{
			if (hlIterating > 0)
				{
				if (hlRemove != null) hlRemove.remove(l);
				if (!hardListeners.contains(l))
					{
					if (hlAdd == null) hlAdd = new HashSet<UpdateListener>();
					hlAdd.add(l);
					}
				}
			else
				hardListeners.add(l);
			}
		}

	public void addListener(UpdateListener l)
		{
		addListener(l,true);
		}

	public void removeListener(UpdateListener l)
		{
		if (wlIterating > 0)
			{
			if (wlAdd != null) wlAdd.remove(l);
			if (weakListeners.containsKey(l))
				{
				if (wlRemove == null) wlRemove = new WeakHashMap<UpdateListener,Boolean>();
				wlRemove.put(l,Boolean.TRUE);
				}
			}
		else
			weakListeners.remove(l);
		if (hlIterating > 0)
			{
			if (hlAdd != null) hlAdd.remove(l);
			if (hardListeners.contains(l))
				{
				if (hlRemove == null) hlRemove = new HashSet<UpdateListener>();
				hlRemove.add(l);
				}
			}
		else
			hardListeners.remove(l);
		}

	private void fireUpdate(UpdateEvent e)
		{
		wlIterating++;
		for (UpdateListener l : weakListeners.keySet())
			l.updated(e);
		if (--wlIterating == 0)
			{
			if (wlRemove != null)
				{
				weakListeners.keySet().removeAll(wlRemove.keySet());
				wlRemove = null;
				}
			if (wlAdd != null)
				{
				weakListeners.putAll(wlAdd);
				wlAdd = null;
				}
			}
		hlIterating++;
		for (UpdateListener l : hardListeners)
			l.updated(e);
		if (--hlIterating == 0)
			{
			if (hlRemove != null)
				{
				hardListeners.removeAll(hlRemove);
				hlRemove = null;
				}
			if (hlAdd != null)
				{
				hardListeners.addAll(hlAdd);
				hlAdd = null;
				}
			}
		}

	public static class UpdateEvent
		{
		public final UpdateSource source;

		public UpdateEvent(UpdateSource s)
			{
			source = s;
			}
		}

	public static class UpdateTrigger
		{
		private UpdateSource source;
		private UpdateEvent event;

		private void setSource(UpdateSource s)
			{
			if (source != null) throw new IllegalStateException();
			source = s;
			}

		public void fire()
			{
			if (event == null) event = new UpdateEvent(source);
			source.fireUpdate(event);
			}

		public void fire(UpdateEvent e)
			{
			source.fireUpdate(e);
			}

		public UpdateEvent getEvent()
			{
			if (event == null) event = new UpdateEvent(source);
			return event;
			}
		}

	public interface UpdateListener
		{
		void updated(UpdateEvent e);
		}
	}
