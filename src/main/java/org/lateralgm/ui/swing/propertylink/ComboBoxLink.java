/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.propertylink;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JComboBox;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ComboBoxLink<K extends Enum<K>, V> extends PropertyLink<K,V> implements
		ActionListener
	{
	public final JComboBox<?> box;
	private final ComboBoxConversion<V> conv;

	public static interface ComboBoxConversion<T>
		{
		/**
		 * Converts a JComboBox selectable item into its backend equivalent.
		 */
		public T convertItem(int index, Object o);
		}

	public static interface ComboBoxSelectable<V>
		{
		public void select(JComboBox<?> b, V o);
		}

	public static class DefaultComboBoxConversion<V> implements ComboBoxConversion<V>,
		ComboBoxSelectable<V>
		{
		@SuppressWarnings("unchecked")
		public V convertItem(int ind, Object o)
			{
			return (V) o;
			}

		public void select(JComboBox<?> b, V o)
			{
			b.setSelectedItem(o);
			}
		}

	public static class IndexComboBoxConversion implements ComboBoxConversion<Integer>,
	ComboBoxSelectable<Integer>
	{
	public Integer convertItem(int ind, Object o)
		{
		return ind;
		}

	public void select(JComboBox<?> b, Integer o)
		{
		b.setSelectedIndex(o);
		}
	}

	public static class KeyComboBoxConversion<V> implements ComboBoxConversion<V>,
	ComboBoxSelectable<V>
	{
		private final V[] items;
		private final Map<?,Integer> map;

		public KeyComboBoxConversion(V[] items) {
			this.items = items;
			this.map = null;
		}

		public KeyComboBoxConversion(V[] items, Map<?,Integer> map) {
			this.items = items;
			this.map = map;
		}

		public V convertItem(int ind, Object o)
			{
			return (V) items[ind];
			}

		public void select(JComboBox<?> b, V o)
			{
			if (map != null) {
				b.setSelectedIndex(map.get(o));
				return;
			}
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals(o)) {
					b.setSelectedIndex(i);
					return;
				}
			}
			}
	}

	public ComboBoxLink(JComboBox<?> b, PropertyMap<K> m, K k, ComboBoxConversion<V> conv)
		{
		super(m,k);
		box = b;
		this.conv = conv == null ? new DefaultComboBoxConversion<V>() : conv;
		reset();
		box.addActionListener(this);
		}

	protected void setComponent(V i)
		{
			setConvertedSelection(i);
		}

	/**
	 * Searches for and selects the selectable item that converts to this backend object.
	 * To make this method more efficient, your ComboBoxConversion should also implement
	 * ComboBoxSelectable
	 */
	public void setConvertedSelection(V o)
		{
		if (conv instanceof ComboBoxSelectable)
			{
			((ComboBoxSelectable<V>) conv).select(box,o);
			return;
			}
		if (o == null) return;
		for (int i = 0; i < box.getItemCount(); i++)
			{
			V it = conv.convertItem(i,box.getItemAt(i));
			if (o.equals(it))
				{
				box.setSelectedIndex(i);
				return;
				}
			}
		}

	public V getConvertedSelection()
		{
			return conv.convertItem(box.getSelectedIndex(),box.getSelectedItem());
		}

	@Override
	public void remove()
		{
		super.remove();
		box.removeActionListener(this);
		}

	@Override
	public void updated(PropertyUpdateEvent<K> e)
		{
		editComponentIfChanged(getConvertedSelection());
		}

	public void actionPerformed(ActionEvent e)
		{
		Object i = getConvertedSelection();
		if (i.equals(map.get(key))) return;
		editProperty(i);
		}

	}
