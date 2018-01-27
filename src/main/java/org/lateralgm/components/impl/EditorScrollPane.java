/*
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 *
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import org.lateralgm.main.Util;
import org.lateralgm.util.PropertyEditor;
import org.lateralgm.util.PropertyLink;
import org.lateralgm.util.PropertyMap;

public class EditorScrollPane extends JScrollPane implements PropertyEditor<Integer>
	{
	private static final long serialVersionUID = 1L;

	private ScrollLink<?> link;

	public EditorScrollPane()
		{
		this(null);
		}

	public EditorScrollPane(Component view)
		{
		super(view);
		verticalScrollBar.setUnitIncrement(16);
		verticalScrollBar.setBlockIncrement(64);
		horizontalScrollBar.setUnitIncrement(16);
		horizontalScrollBar.setBlockIncrement(64);
		}

	public EditorScrollPane(Component view, boolean transparencyBackground)
		{
		this(transparencyBackground ? null : view);
		if (transparencyBackground) { createTransparencyViewport(view); }
		}

	public void createTransparencyViewport(Component view)
		{
		JViewport viewport = new JViewport() {
		/**
		 * NOTE: Default UID generated, change if necessary.
		 */
		private static final long serialVersionUID = -488286791453210520L;

		private BufferedImage componentBackground = null;

		@Override
		public void paintComponent(Graphics g)
			{
			super.paintComponent(g);

			int width = (int)Math.ceil(this.getWidth() / 16f);
			int height = (int)Math.ceil(this.getHeight() / 16f);

			width = width < 1 ? 1 : width;
			height = height < 1 ? 1 : height;

			if (componentBackground == null || componentBackground.getWidth() != width |
				componentBackground.getHeight() != height) {
				componentBackground = Util.paintBackground(width, height);
			}
			g.drawImage(componentBackground, 0, 0,
				componentBackground.getWidth() * 16, componentBackground.getHeight() * 16, null);
			}
		};
		viewport.setView(view);
		this.setViewport(viewport);
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
			// Currently only used for Room zoom. Since RoomFrame
			// only sets components at init, this is never called.
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
