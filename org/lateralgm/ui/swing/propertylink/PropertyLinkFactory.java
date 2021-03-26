/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014 Robert B. Colton
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.ui.swing.propertylink;

import java.beans.ExceptionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.text.Document;

import org.lateralgm.ui.swing.propertylink.ComboBoxLink.DefaultComboBoxConversion;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.ComboBoxConversion;
import org.lateralgm.util.PropertyEditor;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;

public class PropertyLinkFactory<K extends Enum<K>>
	{
	private PropertyMap<K> map;
	private final ExceptionListener exceptionListener;

	/* Necessary for mass disposing links created by the factory */
	private List<PropertyLink<K,?>> mapLinks = new ArrayList<PropertyLink<K,?>>();

	/*
	 * Removes all the links from the map.
	 */
	public void removeAllLinks()
		{
		for (PropertyLink<K,?> link : mapLinks)
			link.remove();
		}

	/*
	 * Removes all the links from the map and also forgets them.
	 * This method is more suitable for disposing so there will
	 * not be strong references preventing garbage collection.
	 */
	public void clearAllLinks()
		{
		removeAllLinks();
		mapLinks.clear();
		}

	/*
	 * Remove all of the links from the previous map and then add
	 * them to the new map, automatically reinitializing all links.
	 */
	public void setMap(PropertyMap<K> m)
		{
		map = m;
		for (PropertyLink<K,?> link : mapLinks)
			link.setMap(map);
		}

	public PropertyLinkFactory(PropertyMap<K> m, ExceptionListener el)
		{
		map = m;
		exceptionListener = el;
		}

	public <L extends PropertyLink<K,?>>L init(L l)
		{
		mapLinks.add(l);
		l.setExceptionListener(exceptionListener);
		return l;
		}

	public <V>PropertyLink<K,V> make(PropertyEditor<V> pe, K k)
		{
		return init(pe.getLink(map,k));
		}

	public <V>ComboBoxLink<K,V> make(JComboBox<?> b, K k)
		{
		return init(new ComboBoxLink<K,V>(b,map,k, new DefaultComboBoxConversion<V>()));
		}

	public <V>ComboBoxLink<K,V> make(JComboBox<?> b, K k, ComboBoxConversion<V> conv)
		{
		return init(new ComboBoxLink<K,V>(b,map,k,conv));
		}

	public <V>ListLink<K,V> make(JList<?> l, K k)
		{
		return init(new ListLink<K,V>(l,map,k));
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
