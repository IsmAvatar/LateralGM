/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

import org.lateralgm.messages.Messages;
import org.lateralgm.util.PropertyEditor;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;
import org.lateralgm.util.PropertyMap.PropertyUpdateEvent;

public class ColorSelect extends JPanel implements ItemSelectable,PropertyEditor<Color>
	{
	private static final long serialVersionUID = 1L;
	private Color selectedColor;

	public ColorSelect()
		{
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		enableEvents(MouseEvent.MOUSE_FIRST);
		}

	public ColorSelect(Color col)
		{
		this();
		setBackground(col);
		selectedColor = col;
		}

	public void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_CLICKED)
			{
			Color newcol = JColorChooser.showDialog(getParent(),
					Messages.getString("ColorSelect.CHOOSE_TITLE"),selectedColor); //$NON-NLS-1$
			if (newcol != null) setSelectedColor(newcol);
			}
		super.processMouseEvent(e);
		}

	protected void fireItemChanged()
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		ItemEvent e = new ItemEvent(this,ItemEvent.ITEM_STATE_CHANGED,selectedColor,ItemEvent.SELECTED);
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ItemListener.class)
				{
				((ItemListener) listeners[i + 1]).itemStateChanged(e);
				}
			}
		}

	public void setSelectedColor(Color selectedColor)
		{
		this.selectedColor = selectedColor;
		setBackground(selectedColor);
		fireItemChanged();
		}

	public Color getSelectedColor()
		{
		return selectedColor;
		}

	public void addItemListener(ItemListener l)
		{
		listenerList.add(ItemListener.class,l);
		}

	public Object[] getSelectedObjects()
		{
		return new Color[] { selectedColor };
		}

	public void removeItemListener(ItemListener l)
		{
		listenerList.remove(ItemListener.class,l);
		}

	public <K extends Enum<K>>PropertyLink<K,Color> getLink(PropertyMap<K> m, K k)
		{
		return new ColorSelectLink<K>(m,k);
		}

	private class ColorSelectLink<K extends Enum<K>> extends PropertyLink<K,Color> implements
			ItemListener
		{
		public ColorSelectLink(PropertyMap<K> m, K k)
			{
			super(m,k);
			reset();
			addItemListener(this);
			}

		@Override
		protected void setComponent(Color c)
			{
			setSelectedColor(c);
			}

		@Override
		public void remove()
			{
			super.remove();
			removeItemListener(this);
			}

		@Override
		public void updated(PropertyUpdateEvent<K> e)
			{
			editComponentIfChanged(selectedColor);
			}

		public void itemStateChanged(ItemEvent e)
			{
			if (selectedColor.equals(map.get(key))) return;
			editProperty(selectedColor);
			}
		}
	}
