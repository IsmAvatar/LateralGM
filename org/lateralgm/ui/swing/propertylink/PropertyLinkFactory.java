/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.propertylink;

import java.beans.ExceptionListener;

import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.text.Document;

import org.lateralgm.ui.swing.propertylink.ComboBoxLink.ComboBoxConversion;
import org.lateralgm.util.PropertyEditor;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;

public class PropertyLinkFactory<K extends Enum<K>>
	{
	private final PropertyMap<K> map;
	private final ExceptionListener exceptionListener;

	public PropertyLinkFactory(PropertyMap<K> m, ExceptionListener el)
		{
		map = m;
		exceptionListener = el;
		}

	public <L extends PropertyLink<K,?>>L init(L l)
		{
		l.setExceptionListener(exceptionListener);
		return l;
		}

	public <V>PropertyLink<K,V> make(PropertyEditor<V> pe, K k)
		{
		return init(pe.getLink(map,k));
		}

	public ComboBoxLink<K> make(JComboBox b, K k, ComboBoxConversion conv)
		{
		return init(new ComboBoxLink<K>(b,map,k,conv));
		}

	public ListLink<K> make(JList l, K k)
		{
		return init(new ListLink<K>(l,map,k));
		}

	public FormattedLink<K> make(JFormattedTextField f, K k)
		{
		return init(new FormattedLink<K>(f,map,k));
		}

	public DocumentLink<K> make(Document d, K k)
		{
		return init(new DocumentLink<K>(d,map,k));
		}

	public ButtonModelLink<K> make(ButtonModel m, K k)
		{
		return init(new ButtonModelLink<K>(m,map,k));
		}

	public ButtonModelLink<K> make(AbstractButton b, K k)
		{
		return init(new ButtonModelLink<K>(b.getModel(),map,k));
		}

	public BoundedRangeLink<K,Integer> make(BoundedRangeModel m, K k)
		{
		return init(new BoundedRangeLink<K,Integer>(m,new BoundedRangeLink.IntegerConverter(),map,k));
		}

	public BoundedRangeLink<K,Double> make(BoundedRangeModel m, K k, double s)
		{
		return init(new BoundedRangeLink<K,Double>(m,new BoundedRangeLink.DoubleConverter(s),map,k));
		}

	public <V extends Enum<V>>ButtonGroupLink<K,V> make(ButtonGroup g, K k, Class<V> vt)
		{
		return init(new ButtonGroupLink<K,V>(g,vt,map,k));
		}

	public ButtonIncrementLink<K,Integer> make(AbstractButton ab, K k, int i, int l)
		{
		return init(ButtonIncrementLink.make(ab,i,l,map,k));
		}
	}
