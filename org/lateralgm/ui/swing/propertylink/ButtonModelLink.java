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

import javax.swing.ButtonModel;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ButtonModelLink<K extends Enum<K>> extends PropertyLink<K,Boolean> implements
		ActionListener
	{
	public final ButtonModel model;

	public ButtonModelLink(ButtonModel bm, PropertyMap<K> m, K k)
		{
		super(m,k);
		model = bm;
		reset();
		bm.addActionListener(this);
		}

	protected void setComponent(Boolean s)
		{
		model.setSelected(s);
		}

	@Override
	public void remove()
		{
		super.remove();
		model.removeActionListener(this);
		}

	public void actionPerformed(ActionEvent e)
		{
		boolean s = model.isSelected();
		if (Boolean.valueOf(s).equals(map.get(key))) return;
		editProperty(s);
		}

	public void updated(PropertyUpdateEvent<K> e)
		{
		editComponentIfChanged(model.isSelected());
		}
	}
