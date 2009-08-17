/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;

import javax.swing.JScrollPane;

import org.lateralgm.util.PropertyEditor;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;

public class EditorScrollPane extends JScrollPane implements PropertyEditor<Integer>
	{
	private static final long serialVersionUID = 1L;

	private ScrollLink<?> link;

	public EditorScrollPane(Component view)
		{
		super(view);
		verticalScrollBar.setUnitIncrement(16);
		verticalScrollBar.setBlockIncrement(64);
		horizontalScrollBar.setUnitIncrement(16);
		horizontalScrollBar.setBlockIncrement(64);
		}

	public void processMouseWheelEvent(MouseWheelEvent e)
		{
		if (link != null && e.isControlDown())
			link.scroll(~e.getWheelRotation() >> 31 | 1); // Scrolls by +/- 1
		else if (e.isShiftDown() || horizontalScrollBar.getMousePosition() != null)
			{
			int a = e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL ? e.getWheelRotation()
					* horizontalScrollBar.getBlockIncrement() : e.getUnitsToScroll()
					* horizontalScrollBar.getUnitIncrement();
			horizontalScrollBar.setValue(horizontalScrollBar.getValue() + a);
			}
		else
			super.processMouseWheelEvent(e);
		}

	public <K extends Enum<K>>PropertyLink<K,Integer> getLink(PropertyMap<K> m, K k)
		{
		return new ScrollLink<K>(m,k);
		}

	private class ScrollLink<K extends Enum<K>> extends PropertyLink<K,Integer>
		{
		public ScrollLink(PropertyMap<K> m, K k)
			{
			super(m,k);
			reset();
			link = ScrollLink.this;
			}

		protected void setComponent(Integer i)
			{
			}

		@Override
		public void remove()
			{
			super.remove();
			if (link == ScrollLink.this) link = null;
			}

		private void scroll(int i)
			{
			int v = map.get(key);
			editProperty(v + i);
			}
		}

	}
