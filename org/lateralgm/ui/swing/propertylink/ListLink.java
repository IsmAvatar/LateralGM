/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.propertylink;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ListLink<K extends Enum<K>, V> extends PropertyLink<K,V> implements
		ListSelectionListener
	{
	public final JList<?> list;

	public ListLink(JList<?> l, PropertyMap<K> m, K k)
		{
		super(m,k);
		list = l;
		reset();
		list.addListSelectionListener(this);
		}

	@Override
	protected void setComponent(Object v)
		{
		list.setSelectedValue(v,true);
		}

	@Override
	public void remove()
		{
		super.remove();
		list.removeListSelectionListener(this);
		}

	@Override
	public void updated(PropertyUpdateEvent<K> e)
		{
		// TODO: Should have a converter interface like ComboBoxLink
		editComponentIfChanged((V)list.getSelectedValue());
		}

	public void valueChanged(ListSelectionEvent e)
		{
		Object v = list.getSelectedValue();
		if (v == null ? map.get(key) == null : v.equals(map.get(key))) return;
		editProperty(v);
		}

	}
