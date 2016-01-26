/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.util.ActiveArrayList.ListUpdateEvent.Type;

public class ActiveArrayList<E> extends ArrayList<E>
	{
	private static final long serialVersionUID = 1L;
	public final UpdateSource updateSource;
	private final UpdateTrigger trigger;

	public ActiveArrayList()
		{
		trigger = new UpdateTrigger();
		updateSource = new UpdateSource(this,trigger);
		}

	public boolean add(E e)
		{
		int i = size();
		super.add(e);
		trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,i,i));
		return true;
		}

	public void add(int index, E element)
		{
		super.add(index,element);
		trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,index,index));
		}

	@Override
	public boolean addAll(Collection<? extends E> c)
		{
		int s = size();
		if (super.addAll(c))
			{
			trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,s,size() - 1));
			return true;
			}
		return false;
		}

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
		{
		int s = size();
		if (super.addAll(index,c))
			{
			trigger.fire(new ListUpdateEvent(updateSource,Type.ADDED,index,index + size() - s - 1));
			return true;
			}
		return false;
		}

	@Override
	public void clear()
		{
		int s = size();
		super.clear();
		trigger.fire(new ListUpdateEvent(updateSource,Type.REMOVED,0,s - 1));
		}

	@Override
	public E remove(int index)
		{
		E e = super.remove(index);
		trigger.fire(new ListUpdateEvent(updateSource,Type.REMOVED,index,index));
		return e;
		}

	@Override
	public boolean remove(Object o)
		{
		int i = indexOf(o);
		if (i >= 0)
			{
			super.remove(i);
			trigger.fire(new ListUpdateEvent(updateSource,Type.REMOVED,i,i));
			return true;
			}
		return false;
		}

	@Override
	public boolean removeAll(Collection<?> c)
		{
		int s = c.size();
		if (s == 0) return false;
		if (s == 1) return remove(c.iterator().next());
		if (super.removeAll(c))
			{
			trigger.fire(new ListUpdateEvent(updateSource,Type.CHANGED,0,Integer.MAX_VALUE));
			return true;
			}
		return false;
		}

	@Override
	public boolean retainAll(Collection<?> c)
		{
		if (super.retainAll(c))
			{
			trigger.fire(new ListUpdateEvent(updateSource,Type.CHANGED,0,Integer.MAX_VALUE));
			return true;
			}
		return false;
		}

	public E set(int index, E element)
		{
		E e = super.set(index,element);
		trigger.fire(new ListUpdateEvent(updateSource,Type.CHANGED,index,index));
		return e;
		}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
		{
		// FIXME Sub list's 'set' method needs overriding
		return super.subList(fromIndex,toIndex);
		}

	public static class ListUpdateEvent extends UpdateEvent
		{
		public enum Type
			{
			ADDED,REMOVED,CHANGED
			}

		public final ListUpdateEvent.Type type;
		public final int fromIndex, toIndex;

		public ListUpdateEvent(UpdateSource s, ListUpdateEvent.Type t, int from, int to)
			{
			super(s);
			type = t;
			fromIndex = from;
			toIndex = to;
			}
		}
	}
