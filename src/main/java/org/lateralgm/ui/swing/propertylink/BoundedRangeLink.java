/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.propertylink;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class BoundedRangeLink<K extends Enum<K>, V extends Number> extends PropertyLink<K,V>
		implements ChangeListener
	{
	public final BoundedRangeModel model;
	public final Converter<V> converter;

	public BoundedRangeLink(BoundedRangeModel brm, Converter<V> c, PropertyMap<K> m, K k)
		{
		super(m,k);
		model = brm;
		converter = c;
		reset();
		model.addChangeListener(this);
		}

	@Override
	protected void setComponent(V v)
		{
		model.setValue(converter.fromProperty(v));
		}

	@Override
	public void remove()
		{
		super.remove();
		model.removeChangeListener(this);
		}

	@Override
	public void updated(PropertyUpdateEvent<K> e)
		{
		V v = map.get(key);
		if (converter.fromProperty(v) == model.getValue()) return;
		editComponent(v);
		}

	public void stateChanged(ChangeEvent e)
		{
		V v = converter.toProperty(model.getValue());
		if (v.equals(map.get(key))) return;
		editProperty(v);
		}

	public abstract static interface Converter<V extends Number>
		{
		V toProperty(int v);

		int fromProperty(V v);
		}

	public static class IntegerConverter implements Converter<Integer>
		{
		public int fromProperty(Integer v)
			{
			return v;
			}

		public Integer toProperty(int v)
			{
			return v;
			}
		}

	public static class DoubleConverter implements Converter<Double>
		{
		public final double step;

		public DoubleConverter(double s)
			{
			step = s;
			}

		public int fromProperty(Double v)
			{
			return (int) Math.round(v * step);
			}

		public Double toProperty(int v)
			{
			return v / step;
			}
		}
	}
