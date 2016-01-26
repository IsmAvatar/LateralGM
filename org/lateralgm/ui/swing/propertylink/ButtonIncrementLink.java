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

import javax.swing.AbstractButton;

import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;

public class ButtonIncrementLink<K extends Enum<K>, V extends Number & Comparable<V>> extends
		PropertyLink<K,V> implements ActionListener
	{
	public final AbstractButton button;
	public final Incrementor<V> incrementor;
	public final V min, max;
	private V value;

	public ButtonIncrementLink(AbstractButton ab, Incrementor<V> i, V min, V max, PropertyMap<K> m,
			K k)
		{
		super(m,k);
		button = ab;
		incrementor = i;
		this.min = min;
		this.max = max;
		reset();
		ab.addActionListener(this);
		}

	@Override
	protected void setComponent(V v)
		{
		value = min.compareTo(v) > 0 ? min : max.compareTo(v) < 0 ? max : v;
		V n = incrementor.increment(value);
		button.setEnabled(min.compareTo(n) <= 0 && max.compareTo(n) >= 0);
		}

	@Override
	public void remove()
		{
		super.remove();
		button.removeActionListener(this);
		}

	public void actionPerformed(ActionEvent e)
		{
		V n = incrementor.increment(value);
		editProperty(n);
		}

	public static <K extends Enum<K>>ButtonIncrementLink<K,Integer> make(AbstractButton ab, int i,
			int l, PropertyMap<K> m, K k)
		{
		return new ButtonIncrementLink<K,Integer>(ab,new IntegerIncrementor(i),i < 0 ? l
				: Integer.MIN_VALUE,i > 0 ? l : Integer.MAX_VALUE,m,k);
		}

	public static interface Incrementor<V extends Number & Comparable<V>>
		{
		V increment(V v);
		}

	public static class IntegerIncrementor implements Incrementor<Integer>
		{
		public final int increment;

		public IntegerIncrementor(int i)
			{
			increment = i;
			}

		public Integer increment(Integer i)
			{
			return i + increment;
			}
		}

	}
