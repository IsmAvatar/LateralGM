/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.propertylink;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class FormattedLink<K extends Enum<K>> extends PropertyLink<K,Object> implements
		PropertyChangeListener
	{
	public final JFormattedTextField field;

	public FormattedLink(JFormattedTextField f, PropertyMap<K> m, K k)
		{
		super(m,k);
		field = f;
		reset();
		field.addPropertyChangeListener("value",this);
		}

	@Override
	protected void setComponent(Object v)
		{
		field.setValue(v);
		}

	@Override
	public void remove()
		{
		super.remove();
		field.removePropertyChangeListener("value",this);
		}

	@Override
	public void updated(PropertyUpdateEvent<K> e)
		{
		editComponentIfChanged(field.getValue());
		}

	public void propertyChange(PropertyChangeEvent evt)
		{
		editProperty(field.getValue());
		}

	}
