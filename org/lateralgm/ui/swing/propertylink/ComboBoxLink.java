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

import javax.swing.JComboBox;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ComboBoxLink<K extends Enum<K>> extends PropertyLink<K,Object> implements
		ActionListener
	{
	public final JComboBox box;
	private final ComboBoxConversion conv;

	public static interface ComboBoxConversion
		{
		/**
		 * Converts a JComboBox selectable item into its backend equivalent.
		 */
		public Object convertItem(int index, Object o);
		}

	public static interface ComboBoxSelectable
		{
		public void select(JComboBox b, Object o);
		}

	public static class DefaultComboBoxConversion implements ComboBoxConversion,ComboBoxSelectable
		{
		public Object convertItem(int ind, Object o)
			{
			return o;
			}

		public void select(JComboBox b, Object o)
			{
			b.setSelectedItem(o);
			}
		}

	public static class IndexComboBoxConversion implements ComboBoxConversion,ComboBoxSelectable
		{
		public Object convertItem(int ind, Object o)
			{
			return ind;
			}

		public void select(JComboBox b, Object o)
			{
			if (o instanceof Integer) b.setSelectedIndex((Integer) o);
			}
		}

	public ComboBoxLink(JComboBox b, PropertyMap<K> m, K k, ComboBoxConversion conv)
		{
		super(m,k);
		box = b;
		this.conv = conv == null ? new DefaultComboBoxConversion() : conv;
		reset();
		box.addActionListener(this);
		}

	protected void setComponent(Object i)
		{
		setConvertedSelection(i);
		}

	/**
	 * Searches for and selects the selectable item that converts to this backend object.
	 * To make this method more efficient, your ComboBoxConversion should also implement ComboBoxSelectable
	 */
	public void setConvertedSelection(Object o)
		{
		if (conv instanceof ComboBoxSelectable)
			{
			((ComboBoxSelectable) conv).select(box,o);
			return;
			}
		if (o == null) return;
		for (int i = 0; i < box.getItemCount(); i++)
			{
			Object it = conv.convertItem(i,box.getItemAt(i));
			if (o.equals(it))
				{
				box.setSelectedIndex(i);
				return;
				}
			}
		}

	public Object getConvertedSelection()
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
