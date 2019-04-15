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
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ButtonGroupLink<K extends Enum<K>, V extends Enum<V>> extends PropertyLink<K,V>
		implements ActionListener
	{
	public final ButtonGroup group;
	public final V[] values;
	private final EnumMap<V,AbstractButton> buttons;
	private final Map<ButtonModel,V> buttonValues;

	public ButtonGroupLink(ButtonGroup g, Class<V> vt, PropertyMap<K> m, K k)
		{
		super(m,k);
		values = vt.getEnumConstants();
		if (g.getButtonCount() != values.length) throw new IllegalArgumentException();
		group = g;
		buttons = new EnumMap<V,AbstractButton>(vt);
		buttonValues = new HashMap<ButtonModel,V>(values.length);
		Enumeration<AbstractButton> e = g.getElements();
		for (V v : values)
			{
			AbstractButton b = e.nextElement();
			buttonValues.put(b.getModel(),v);
			buttons.put(v,b);
			}
		reset();
		for (AbstractButton b : buttons.values())
			b.addActionListener(this);
		}

	@Override
	protected void setComponent(V v)
		{
		if (v == null)
			group.clearSelection();
		else
			group.setSelected(buttons.get(v).getModel(),true);
		}

	@Override
	public void remove()
		{
		super.remove();
		for (AbstractButton b : buttons.values())
			b.removeActionListener(this);
		}

	@Override
	public void updated(PropertyUpdateEvent<K> e)
		{
		V v = map.get(key);
		if (buttons.get(v).getModel() == group.getSelection()) return;
		editComponent(v);
		}

	public void actionPerformed(ActionEvent e)
		{
		V v = buttonValues.get(group.getSelection());
		if (v == map.get(key)) return;
		editProperty(v);
		}
	}
