/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.util;

import java.beans.ExceptionListener;

import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;
import org.lateralgm.util.PropertyMap.PropertyUpdateListener;

public abstract class PropertyLink<K extends Enum<K>, V> extends PropertyUpdateListener<K>
	{
	protected PropertyMap<K> map;
	protected final K key;
	private ExceptionListener exceptionListener;

	public PropertyLink(PropertyMap<K> m, K k)
		{
		map = m;
		key = k;
		m.updateSource.addListener(this);
		}

	public void remove()
		{
		map.updateSource.removeListener(this);
		}

	public void setMap(PropertyMap<K> m)
		{
		// does not call this.remove() because some subclasses
		// override it and remove the action listener and etc
		// which we do not want when only swapping maps
		map.updateSource.removeListener(this);
		map = m; // << change the map now
		reset(); // << synchronize component to map value
		// finally, start listening for map changes again
		map.updateSource.addListener(this);
		}

	protected abstract void setComponent(V v);

	protected void reset()
		{
		V v = map.get(key);
		editComponent(v);
		}

	protected void editComponentIfChanged(V old)
		{
		V v = map.get(key);
		if (v == null ? old == null : v.equals(old)) return;
		editComponent(v);
		}

	protected void editComponent(V v)
		{
		setComponent(v);
		}

	protected void editProperty(Object v)
		{
		try
			{
			map.put(key,v);
			}
		catch (RuntimeException re)
			{
			reset();
			if (exceptionListener != null)
				exceptionListener.exceptionThrown(re);
			else
				throw re;
			}
		}

	public void setExceptionListener(ExceptionListener l)
		{
		exceptionListener = l;
		}

	@Override
	public void updated(PropertyUpdateEvent<K> e)
		{
		if (e.key != this.key) return;
		V v = map.get(key);
		editComponent(v);
		}

	public static void removeAll(PropertyLink<?,?>...links)
		{
		for (PropertyLink<?,?> l : links)
			if (l != null) l.remove();
		}
	}
