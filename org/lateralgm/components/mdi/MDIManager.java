/*
 * Copyright (C) 2007 Clam <clamisgood@gmail.com>
 * Copyright (C) 2007 IsmAvatar <IsmAvatar@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

public class MDIManager extends DefaultDesktopManager
	{
	private static final long serialVersionUID = 1L;
	protected MDIPane pane;
	private JScrollPane scroll;
	private CListener cListener = new CListener();
	/**prevents recursion*/
	public boolean resizing = false;

	public void setScrollPane(JScrollPane scroll)
		{
		this.scroll = scroll;
		scroll.addComponentListener(cListener);
		resizeDesktop();
		}

	public MDIManager(MDIPane pane)
		{
		super();
		this.pane = pane;
		}

	public void endResizingFrame(JComponent f)
		{
		super.endResizingFrame(f);
		resizeDesktop();
		}

	public void endDraggingFrame(JComponent f)
		{
		super.endDraggingFrame(f);
		resizeDesktop();
		}

	/**
	 * Resizes the desktop to reflect the frame (and icon)
	 * positions. Also repositions the frames so that empty
	 * spaces are removed while preserving the current view
	 * (similar behaviour to the windows MDI).
	 */
	public void resizeDesktop()
		{
		if (!resizing && scroll != null)
			{
			resizing = true;

			Rectangle viewrect = scroll.getViewport().getViewRect();
			int xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE, xmax = 0, ymax = 0;
			for (JInternalFrame f : pane.getAllFrames())
				{
				if (f.isVisible())
					{
					if (!f.isMaximum())
						{
						JComponent comp;
						if (!f.isIcon())
							comp = f;
						else
							comp = f.getDesktopIcon();
						xmin = Math.min(comp.getX(),xmin);
						ymin = Math.min(comp.getY(),ymin);
						xmax = Math.max(comp.getX() + comp.getWidth(),xmax);
						ymax = Math.max(comp.getY() + comp.getHeight(),ymax);
						}
					else
						{
						pane.setPreferredSize(viewrect.getSize());
						pane.getParent().invalidate();
						pane.getParent().validate();
						resizing = false;
						return;
						}
					}
				}
			int xcorrect = 0, ycorrect = 0;
			if (viewrect.x < xmin)
				xcorrect = -viewrect.x;
			else
				xcorrect = -xmin;

			if (viewrect.y < ymin)
				ycorrect = -viewrect.y;
			else
				ycorrect = -ymin;

			Point newviewpos = new Point(viewrect.x + xcorrect,viewrect.y + ycorrect);

			Dimension newPaneSize = new Dimension(
					Math.max(xmax + xcorrect,newviewpos.x + viewrect.width),Math.max(ymax + ycorrect,
							newviewpos.y + viewrect.height));

			for (JInternalFrame f : pane.getAllFrames())
				{
				if (!f.isIcon())
					{
					Point p = f.getLocation();
					f.setLocation(new Point(p.x + xcorrect,p.y + ycorrect));
					}
				Point p = f.getDesktopIcon().getLocation();
				f.getDesktopIcon().setLocation(new Point(p.x + xcorrect,p.y + ycorrect));
				f.repaint();
				}

			scroll.getViewport().setViewPosition(newviewpos);
			pane.setPreferredSize(newPaneSize);

			pane.getParent().invalidate();
			pane.getParent().validate();
			pane.repaint();
			resizing = false;
			}
		}

	private class CListener extends ComponentAdapter
		{
		public CListener()
			{
			super();
			}

		public void componentResized(ComponentEvent e)
			{
			resizeDesktop();
			for (JInternalFrame f : pane.getAllFrames())
				if (f.isMaximum()) f.setSize(pane.getPreferredSize());
			}
		}
	}
