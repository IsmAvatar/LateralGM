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

	public ComboBoxLink(JComboBox b, PropertyMap<K> m, K k)
		{
		super(m,k);
		box = b;
		reset();
		box.addActionListener(this);
		}

	protected void setComponent(Object i)
		{
		box.setSelectedItem(i);
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
		editComponentIfChanged(box.getSelectedItem());
		}

	public void actionPerformed(ActionEvent e)
		{
		Object i = box.getSelectedItem();
		if (i.equals(map.get(key))) return;
		editProperty(i);
		}

	}
