/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.util;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;

public class PropertyMap<K extends Enum<K>> extends EnumMap<K,Object>
	{
	private static final long serialVersionUID = 1L;

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);
	private EnumMap<K,TriggerSourcePair> updatePairs;
	private final Class<K> keyType;
	private final PropertyValidator<K> validator;

	public PropertyMap(Class<K> type, PropertyValidator<K> v, EnumMap<K,Object> defaults)
		{
		super(defaults == null ? new EnumMap<K,Object>(type) : defaults);
		keyType = type;
		validator = v;
		}

	public UpdateSource getUpdateSource(K key)
		{
		TriggerSourcePair p = null;
		if (updatePairs == null)
			updatePairs = new EnumMap<K,TriggerSourcePair>(keyType);
		else
			p = updatePairs.get(key);
		if (p == null)
			{
			p = new TriggerSourcePair();
			updatePairs.put(key,p);
			}
		return p.source;
		}

	protected void fireUpdate(K k)
		{
		PropertyUpdateEvent<K> e = new PropertyUpdateEvent<K>(updateSource,this,k);
		updateTrigger.fire(e);
		if (updatePairs != null)
			{
			TriggerSourcePair p = updatePairs.get(k);
			if (p != null) p.trigger.fire(e);
			}
		}

	@SuppressWarnings("unchecked")
	public <V>V get(K key)
		{
		return (V) super.get(key);
		}

	@Override
	public void clear()
		{
		throw new UnsupportedOperationException();
		}

	public Object put(K key, Object value)
		{
		boolean ck = super.containsKey(key);
		if (ck) if (super.get(key) == value) return value;
		Object vv = validator == null ? value : validator.validate(key,value);
		Object o = super.put(key,vv);
		if (!ck || vv != value || o != value) fireUpdate(key);
		return o;
		}

	@Override
	public Set<java.util.Map.Entry<K,Object>> entrySet()
		{
		return Collections.unmodifiableSet(super.entrySet());
		}

	@Override
	public void putAll(Map<? extends K,? extends Object> m)
		{
		for (Map.Entry<? extends K,? extends Object> e : m.entrySet())
			put(e.getKey(),e.getValue());
		}

	@Override
	public PropertyMap<K> clone()
		{
		return (PropertyMap<K>) super.clone();
		}

	@SuppressWarnings("unchecked")
	@Override
	public Object remove(Object key)
		{
		int s = size();
		Object o = super.remove(key);
		// We know that this cast to K is valid if o != null.
		if (o != null || s != size()) fireUpdate((K) key);
		return o;
		}

	@Override
	public Collection<Object> values()
		{
		return Collections.unmodifiableCollection(super.values());
		}

	public static <K extends Enum<K>>EnumMap<K,Object> makeDefaultMap(Class<K> type, Object...values)
		{
		K[] ec = type.getEnumConstants();
		if (ec.length != values.length) throw new IllegalArgumentException();
		EnumMap<K,Object> m = new EnumMap<K,Object>(type);
		for (K k : ec)
			m.put(k,values[k.ordinal()]);
		return m;
		}

	public static class PropertyUpdateEvent<K extends Enum<K>> extends UpdateEvent
		{
		public final PropertyMap<K> map;
		public final K key;

		public PropertyUpdateEvent(UpdateSource s, PropertyMap<K> m, K k)
			{
			super(s);
			map = m;
			key = k;
			}
		}

	public abstract static class PropertyUpdateListener<K extends Enum<K>> implements UpdateListener
		{
		@SuppressWarnings("unchecked")
		public void updated(UpdateEvent e)
			{
			updated((PropertyUpdateEvent<K>) e);
			}

		public abstract void updated(PropertyUpdateEvent<K> e);
		}

	private class TriggerSourcePair
		{
		public final UpdateTrigger trigger;
		public final UpdateSource source;

		public TriggerSourcePair()
			{
			trigger = new UpdateTrigger();
			source = new UpdateSource(PropertyMap.this,trigger);
			}
		}

	public static interface PropertyValidator<K extends Enum<K>>
		{
		Object validate(K k, Object v);
		}

	public static class PropertyValidationException extends IllegalArgumentException
		{
		private static final long serialVersionUID = 1L;
		}
	}
